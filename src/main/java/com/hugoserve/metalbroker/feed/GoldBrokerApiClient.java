package com.hugoserve.metalbroker.feed;

import com.hugoserve.metalbroker.proto.MetalRatesProto;

public interface GoldBrokerApiClient {
    MetalRatesProto.SpotPrice fetchCurrent(String metalCode, String currency, String unit);
    MetalRatesProto.SpotPricesResponse fetchIntraday(String metalCode, String currency, String unit);
    MetalRatesProto.HistoricalSpotPricesResponse fetchDailyHistory(String metalCode, String currency, String unit);
}
