package com.hugoserve.metalbroker.service;

import com.hugoserve.metalbroker.proto.MetalRatesProto;
import com.hugoserve.metalbroker.proto.MetalRatesProto.Currency;
import com.hugoserve.metalbroker.proto.MetalRatesProto.Metal;
import com.hugoserve.metalbroker.proto.MetalRatesProto.WeightUnit;

public interface RateService {

    MetalRatesProto.SpotPrice getLiveRate(
            Metal metal,
            Currency currency,
            WeightUnit weightUnit
    );

    MetalRatesProto.SpotPricesResponse getTodayRates(
            Metal metal,
            Currency currency,
            WeightUnit weightUnit,
            Integer page,
            Integer limit
    );

    MetalRatesProto.HistoricalSpotPricesResponse getHistoricalRates(
            Metal metal,
            Currency currency,
            WeightUnit weightUnit,
            Integer page,
            Integer limit
    );
}

