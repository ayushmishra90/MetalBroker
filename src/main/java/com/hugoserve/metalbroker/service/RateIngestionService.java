package com.hugoserve.metalbroker.service;

import com.hugoserve.metalbroker.proto.MetalRatesProto;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RateIngestionService {

    void ingestLatestAndTick(String metalCode, MetalRatesProto.SpotPrice dto);

    void ingestIntraday(String metalCode, List<MetalRatesProto.SpotPrice> items);

    void ingestDailyHistory(
            String metalCode,
            List<MetalRatesProto.SpotPriceItem> items,
            int lastNDaysOnly
    );

    long cleanupOldIntraday(int retentionDays);

    Optional<Instant> getLatestFeedTsUtcUncached(String metal);

    Optional<Instant> getFeedTsUtc(String metalCode);

}
