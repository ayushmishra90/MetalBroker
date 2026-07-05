package com.hugoserve.metalbroker.service.impl;

import com.hugoserve.metalbroker.facade.RateFacade;
import com.hugoserve.metalbroker.proto.MetalRatesProto;
import com.hugoserve.metalbroker.service.RateService;
import com.hugoserve.metalbroker.utils.exception.BusinessException;
import org.springframework.stereotype.Service;

@Service
public class RateServiceImpl implements RateService {

    private final RateFacade facade;

    public RateServiceImpl(RateFacade facade) {
        this.facade = facade;
    }

    @Override
    public MetalRatesProto.SpotPrice getLiveRate(
            MetalRatesProto.Metal metal,
            MetalRatesProto.Currency currency,
            MetalRatesProto.WeightUnit weightUnit
    ) {
        validate(metal, currency, weightUnit);
        return facade.fetchLiveRate(metal, currency, weightUnit);
    }

    @Override
    public MetalRatesProto.SpotPricesResponse getTodayRates(
            MetalRatesProto.Metal metal,
            MetalRatesProto.Currency currency,
            MetalRatesProto.WeightUnit weightUnit,
            Integer page,
            Integer limit
    ) {
        validate(metal, currency, weightUnit);
        return facade.fetchTodayRates(metal, currency, weightUnit, page, limit);
    }

    @Override
    public MetalRatesProto.HistoricalSpotPricesResponse getHistoricalRates(
            MetalRatesProto.Metal metal,
            MetalRatesProto.Currency currency,
            MetalRatesProto.WeightUnit weightUnit,
            Integer page,
            Integer limit
    ) {
        validate(metal, currency, weightUnit);
        return facade.fetchHistoricalRates(metal, currency, weightUnit, page, limit);
    }

    private void validate(
            MetalRatesProto.Metal metal,
            MetalRatesProto.Currency currency,
            MetalRatesProto.WeightUnit weightUnit) {

        if (metal == MetalRatesProto.Metal.METAL_UNSPECIFIED) {
            throw new BusinessException(
                    "RATE_INVALID",
                    "metal required"
            );
        }

        if (currency == MetalRatesProto.Currency.CURRENCY_UNSPECIFIED) {
            throw new BusinessException(
                    "RATE_INVALID",
                    "currency required"
            );
        }

        if (weightUnit == MetalRatesProto.WeightUnit.WEIGHT_UNIT_UNSPECIFIED) {
            throw new BusinessException(
                    "RATE_INVALID",
                    "weight unit required"
            );
        }
    }
}
