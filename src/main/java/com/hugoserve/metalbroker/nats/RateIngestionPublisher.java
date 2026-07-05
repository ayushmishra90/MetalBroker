package com.hugoserve.metalbroker.nats;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import org.springframework.stereotype.Component;

@Component
public class RateIngestionPublisher {

    private final Connection nc;
    private final ObjectMapper mapper = new ObjectMapper();

    public RateIngestionPublisher(Connection nc) {
        this.nc = nc;
    }

    public void publish(String subject, Object payload) {
        try {
            nc.publish(subject, mapper.writeValueAsBytes(payload));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
