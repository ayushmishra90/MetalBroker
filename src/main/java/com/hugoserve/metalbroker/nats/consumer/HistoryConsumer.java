package com.hugoserve.metalbroker.nats.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hugoserve.metalbroker.feed.GoldBrokerApiClient;
import com.hugoserve.metalbroker.nats.NatsSubjects;
import com.hugoserve.metalbroker.nats.RateIngestionPublisher;
import com.hugoserve.metalbroker.nats.model.HistoryJob;
import com.hugoserve.metalbroker.nats.RetryPolicy;
import com.hugoserve.metalbroker.service.RateIngestionService;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HistoryConsumer {

    private final Connection nc;
    private final GoldBrokerApiClient api;
    private final RateIngestionService ingestion;
    private final RateIngestionPublisher publisher;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String currency;
    private final String unit;

    public HistoryConsumer(
            Connection nc,
            GoldBrokerApiClient api,
            RateIngestionService ingestion,
            RateIngestionPublisher publisher,
            @Value("${metalbroker.feed.currency}") String currency,
            @Value("${metalbroker.feed.weight-unit}") String unit
    ) {
        this.nc = nc;
        this.api = api;
        this.ingestion = ingestion;
        this.publisher = publisher;
        this.currency = currency;
        this.unit = unit;
    }

    @PostConstruct
    public void subscribe() {
        Dispatcher dispatcher = nc.createDispatcher(this::onMessage);
        dispatcher.subscribe(NatsSubjects.HISTORY);
        System.out.println("Subscribed to NATS History subject: " + NatsSubjects.HISTORY);
    }

    private void onMessage(Message msg) {
        try {

            System.out.println("Processing message from NATS History subject: " + msg);
            HistoryJob job =
                    mapper.readValue(msg.getData(), HistoryJob.class);

            var resp = api.fetchDailyHistory(
                    job.metalCode(),
                    currency,
                    unit
            );

            if (resp != null && resp.hasEmbedded()) {
                ingestion.ingestDailyHistory(
                        job.metalCode(),
                        resp.getEmbedded().getItemsList(),
                        0
                );
            }

        } catch (Exception e) {

            System.out.println("Retrying to NATS History subject: " + e);
            retry(msg);
        }
    }

    private void retry(Message msg) {
        try {
            HistoryJob job =
                    mapper.readValue(msg.getData(), HistoryJob.class);

            if (job.attempt() < RetryPolicy.HISTORY_MAX) {
                publisher.publish(
                        NatsSubjects.HISTORY,
                        job.next()
                );
            }
        } catch (Exception ignored) {
            // permanently drop
        }
    }
}
