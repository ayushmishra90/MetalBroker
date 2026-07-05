package com.hugoserve.metalbroker.nats;

public interface RetryPolicy {
    int INTRADAY_MAX = 1;
    int HISTORY_MAX  = 1;
}
