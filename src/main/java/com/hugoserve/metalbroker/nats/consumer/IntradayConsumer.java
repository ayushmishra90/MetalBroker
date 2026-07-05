package com.hugoserve.metalbroker.nats.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hugoserve.metalbroker.feed.GoldBrokerApiClient;
import com.hugoserve.metalbroker.nats.NatsSubjects;
import com.hugoserve.metalbroker.nats.RateIngestionPublisher;
import com.hugoserve.metalbroker.nats.model.IntradayJob;
import com.hugoserve.metalbroker.nats.RetryPolicy;
import com.hugoserve.metalbroker.service.RateIngestionService;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class IntradayConsumer {

    private final Connection nc;
    private final GoldBrokerApiClient api;
    private final RateIngestionService ingestion;
    private final RateIngestionPublisher publisher;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String currency;
    private final String unit;

    public IntradayConsumer(
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
        dispatcher.subscribe(NatsSubjects.INTRADAY);
        System.out.println("Subscribed to NATS Intraday subject: " + NatsSubjects.INTRADAY);
    }

    private void onMessage(Message msg) {
        try {
            System.out.println("Processing message from Intraday consumer: " + msg);
            IntradayJob job =
                    mapper.readValue(msg.getData(), IntradayJob.class);

            var resp = api.fetchIntraday(
                    job.metalCode(),
                    currency,
                    unit
            );

            if (resp != null && resp.hasEmbedded()) {
                ingestion.ingestIntraday(
                        job.metalCode(),
                        resp.getEmbedded().getItemsList()
                );
            }

        } catch (Exception e) {
            System.out.println("Retrying message for Intraday subject: " + e);
            retry(msg);
        }
    }

    private void retry(Message msg) {
        try {
            IntradayJob job =
                    mapper.readValue(msg.getData(), IntradayJob.class);

            if (job.attempt() < RetryPolicy.INTRADAY_MAX) {
                publisher.publish(
                        NatsSubjects.INTRADAY,
                        job.next()
                );
            }
        } catch (Exception ignored) {
            // permanently drop
        }
    }
}
