package com.hugoserve.metalbroker.schedulers;

import com.hugoserve.metalbroker.feed.GoldBrokerApiClient;
import com.hugoserve.metalbroker.nats.NatsSubjects;
import com.hugoserve.metalbroker.nats.RateIngestionPublisher;
import com.hugoserve.metalbroker.nats.model.CleanupJob;
import com.hugoserve.metalbroker.nats.model.HistoryJob;
import com.hugoserve.metalbroker.nats.model.IntradayJob;
import com.hugoserve.metalbroker.proto.MetalRatesProto;
import com.hugoserve.metalbroker.service.RateIngestionService;
import com.hugoserve.metalbroker.utils.MetalParamMapper;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.hugoserve.metalbroker.config.RedisCacheConfig.CACHE_DB_LATEST;

@Component
public class FeedSchedulers {

    private final GoldBrokerApiClient api;
    private final RateIngestionService ingestion;
    private final RateIngestionPublisher publisher;

    private final List<MetalRatesProto.Metal> metals;
    private final String currency;
    private final String unit;

    /** Blocks scheduled jobs until startup sync finishes */
    private final CountDownLatch startupGate = new CountDownLatch(1);

    /** Small pool – schedulers only enqueue */
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    /** Gap threshold */
    private static final Duration GAP_THRESHOLD = Duration.ofMinutes(10);

    public FeedSchedulers(
            GoldBrokerApiClient api,
            RateIngestionService ingestion,
            RateIngestionPublisher publisher,
            @Value("${metalbroker.feed.currency}") String currency,
            @Value("${metalbroker.feed.weight-unit}") String unit
    ) {
        this.api = api;
        this.ingestion = ingestion;
        this.publisher = publisher;
        this.currency = currency;
        this.unit = unit;

        this.metals = List.of(
                MetalRatesProto.Metal.XAU,
                MetalRatesProto.Metal.XAG,
                MetalRatesProto.Metal.XPT,
                MetalRatesProto.Metal.XPD
        );
    }

    // =========================================================================
    // 1️⃣ STARTUP SYNC (LATEST ONLY + GAP CHECK)
    // =========================================================================
    @CacheEvict(cacheNames = CACHE_DB_LATEST, allEntries = true)
    @EventListener(ApplicationReadyEvent.class)
    public void startupSync() {
        try {
            System.out.println("🚀 Startup Sync Started");

            for (var metal : metals) {
                executor.submit(() -> startupForMetal(metal));
            }

        } finally {
            startupGate.countDown();
            System.out.println("✅ Startup Sync Finished");
        }
    }

    private void startupForMetal(MetalRatesProto.Metal metal) {
        String code = MetalParamMapper.toApi(metal);

        try {
            var latest = api.fetchCurrent(code, currency, unit);
            if (latest == null) return;

            // 1️⃣ Always ingest latest synchronously
            ingestion.ingestLatestAndTick(code, latest);

            publisher.publish(
                    NatsSubjects.INTRADAY,
                    new IntradayJob(code, 0)
            );

        } catch (Exception e) {
            System.err.println("Startup failed for " + metal + ": " + e.getMessage());
        }
    }

    // =========================================================================
    // 2️⃣ 5‑MIN LATEST SYNC (NO INTRADAY HERE)
    // =========================================================================
    @CacheEvict(cacheNames = CACHE_DB_LATEST, allEntries = true)
    @Scheduled(cron = "40 */5 * * * *", zone = "UTC")
    public void syncEvery5Minutes() {
        if (!waitForStartup()) return;

        for (var metal : metals) {
            executor.submit(() -> syncLatestForMetal(metal));
        }
    }

    private void syncLatestForMetal(MetalRatesProto.Metal metal) {
        String code = MetalParamMapper.toApi(metal);

        try {
            var latest = api.fetchCurrent(code, currency, unit);
            if (latest == null) return;

            Instant lastTickTs =
                    ingestion.getLatestFeedTsUtcUncached(code)
                            .orElse(Instant.EPOCH);

            Instant apiTs = Instant.ofEpochSecond(
                    latest.getDate().getSeconds(),
                    latest.getDate().getNanos()
            );

            ingestion.ingestLatestAndTick(code, latest);

            // Gap healing via NATS
            if (Duration.between(lastTickTs, apiTs).compareTo(GAP_THRESHOLD) > 0) {
                publisher.publish(
                        NatsSubjects.INTRADAY,
                        new IntradayJob(code, 0)
                );
            }

        } catch (Exception e) {
            System.err.println("5‑Min sync failed for " + metal + ": " + e.getMessage());
        }
    }

    // =========================================================================
    // 3️⃣ DAILY INTRADAY BACKFILL → NATS ONLY
    // =========================================================================
    @Scheduled(cron = "0 58 21 * * *", zone = "UTC")
    public void dailyIntradaySync() {
        if (!waitForStartup()) return;

        for (var metal : metals) {
            publisher.publish(
                    NatsSubjects.INTRADAY,
                    new IntradayJob(
                            MetalParamMapper.toApi(metal),
                            0
                    )
            );
        }
    }

    // =========================================================================
    // 4️⃣ DAILY HISTORY → NATS
    // =========================================================================
    @Scheduled(cron = "0 3 22 * * *", zone = "UTC")
    public void dailyHistorySync() {
        if (!waitForStartup()) return;

        for (var metal : metals) {
            publisher.publish(
                    NatsSubjects.HISTORY,
                    new HistoryJob(MetalParamMapper.toApi(metal), 0)
            );
        }
    }

    // =========================================================================
    // 5️⃣ CLEANUP → NATS
    // =========================================================================
    @Scheduled(cron = "0 5 22 * * *", zone = "UTC")
    public void cleanupIntraday() {
        if (!waitForStartup()) return;

        publisher.publish(
                NatsSubjects.CLEANUP,
                new CleanupJob()
        );
    }

    // =========================================================================
    // INTERNALS
    // =========================================================================
    private boolean waitForStartup() {
        try {
            return startupGate.await(5, java.util.concurrent.TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }
}
