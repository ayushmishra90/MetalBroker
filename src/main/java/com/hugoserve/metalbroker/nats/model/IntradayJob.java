package com.hugoserve.metalbroker.nats.model;

public record IntradayJob(String metalCode, int attempt) {
    public IntradayJob next() {
        return new IntradayJob(metalCode, attempt + 1);
    }
}
