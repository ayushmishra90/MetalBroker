package com.hugoserve.metalbroker.nats;

public interface NatsSubjects {
    String INTRADAY = "rates.intraday";
    String HISTORY  = "rates.history";
    String CLEANUP  = "rates.cleanup";
}
