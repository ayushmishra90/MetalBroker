package com.hugoserve.metalbroker.controller;

import com.google.protobuf.util.JsonFormat;
import com.hugoserve.metalbroker.proto.MetalRatesProto;
import com.hugoserve.metalbroker.proto.MetalRatesProto.Currency;
import com.hugoserve.metalbroker.proto.MetalRatesProto.Metal;
import com.hugoserve.metalbroker.proto.MetalRatesProto.WeightUnit;
import com.hugoserve.metalbroker.service.RateService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rates")
public class RateController {

    private final RateService rateService;

    public RateController(RateService rateService) {
        this.rateService = rateService;
    }

    // ---------- LIVE ----------

    @GetMapping(value = "/live", produces = "application/x-protobuf")
    public MetalRatesProto.SpotPrice live(
            @RequestParam Metal metal,
            @RequestParam Currency currency,
            @RequestParam(name = "weight_unit") WeightUnit weightUnit
    ) {
        return rateService.getLiveRate(metal, currency, weightUnit);
    }

    @GetMapping(value = "/live/debug", produces = MediaType.APPLICATION_JSON_VALUE)
    public String liveDebug(
            @RequestParam Metal metal,
            @RequestParam Currency currency,
            @RequestParam(name = "weight_unit") WeightUnit weightUnit
    ) throws Exception {
        return JsonFormat.printer()
                .preservingProtoFieldNames()
                .print(rateService.getLiveRate(metal, currency, weightUnit));
    }

    // ---------- TODAY ----------

    @GetMapping(value = "/today", produces = "application/x-protobuf")
    public MetalRatesProto.SpotPricesResponse today(
            @RequestParam Metal metal,
            @RequestParam Currency currency,
            @RequestParam(name = "weight_unit") WeightUnit weightUnit,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit
    ) {
        return rateService.getTodayRates(metal, currency, weightUnit, page, limit);
    }

    @GetMapping(value = "/today/debug", produces = MediaType.APPLICATION_JSON_VALUE)
    public String todayDebug(
            @RequestParam Metal metal,
            @RequestParam Currency currency,
            @RequestParam(name = "weight_unit") WeightUnit weightUnit,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit
    ) throws Exception {
        return JsonFormat.printer()
                .includingDefaultValueFields()
                .preservingProtoFieldNames()
                .print(rateService.getTodayRates(metal, currency, weightUnit, page, limit));
    }

    // ---------- HISTORICAL ----------

    @GetMapping(value = "/historical", produces = "application/x-protobuf")
    public MetalRatesProto.HistoricalSpotPricesResponse historical(
            @RequestParam Metal metal,
            @RequestParam Currency currency,
            @RequestParam(name = "weight_unit") WeightUnit weightUnit,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit
    ) {
        return rateService.getHistoricalRates(metal, currency, weightUnit, page, limit);
    }

    @GetMapping(value = "/historical/debug", produces = MediaType.APPLICATION_JSON_VALUE)
    public String historicalDebug(
            @RequestParam Metal metal,
            @RequestParam Currency currency,
            @RequestParam(name = "weight_unit") WeightUnit weightUnit,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit
    ) throws Exception {
        return JsonFormat.printer()
                .includingDefaultValueFields()
                .preservingProtoFieldNames()
                .print(rateService.getHistoricalRates(metal, currency, weightUnit, page, limit));
    }
}
