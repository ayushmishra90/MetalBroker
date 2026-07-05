package com.hugoserve.metalbroker.nats.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hugoserve.metalbroker.nats.NatsSubjects;
import com.hugoserve.metalbroker.nats.model.CleanupJob;
import com.hugoserve.metalbroker.service.RateIngestionService;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CleanupConsumer {

    private final Connection nc;
    private final RateIngestionService ingestion;
    private final ObjectMapper mapper = new ObjectMapper();
    private final int retentionDays;

    public CleanupConsumer(
            Connection nc,
            RateIngestionService ingestion,
            @Value("${metalbroker.feed.intraday-retention-days}") int retentionDays
    ) {
        this.nc = nc;
        this.ingestion = ingestion;
        this.retentionDays = retentionDays;
    }

    @PostConstruct
    public void subscribe() {
        Dispatcher dispatcher = nc.createDispatcher(this::onMessage);
        dispatcher.subscribe(NatsSubjects.CLEANUP);
        System.out.println("Subscribed to NATS Cleanup subject: " + NatsSubjects.CLEANUP);
    }

    private void onMessage(Message msg) {
        try {

            System.out.println("Processing messages from NATS Cleanup subject: " + msg);
            CleanupJob job = mapper.readValue(msg.getData(), CleanupJob.class);

            ingestion.cleanupOldIntraday(retentionDays);

        } catch (Exception ignored) {
            // cleanup is best-effort

            System.out.println("Exception messages of NATS Cleanup subject: " + ignored);
        }
    }
}
