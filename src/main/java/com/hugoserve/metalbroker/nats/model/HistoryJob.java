package com.hugoserve.metalbroker.nats.model;

public record HistoryJob(String metalCode, int attempt) {
    public HistoryJob next() {
        return new HistoryJob(metalCode, attempt + 1);
    }
}