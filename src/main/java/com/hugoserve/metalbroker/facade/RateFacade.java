package com.hugoserve.metalbroker.facade;

import com.google.protobuf.util.JsonFormat;
import com.hugoserve.metalbroker.proto.MetalRatesProto;
import com.hugoserve.metalbroker.utils.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class RateFacade {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public RateFacade(
            RestTemplate restTemplate,
            @Value("${metalbroker.feed.base-url:https://goldbroker.com/api}") String baseUrl
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    // ---------------- LIVE ----------------

    public MetalRatesProto.SpotPrice fetchLiveRate(
            MetalRatesProto.Metal metal,
            MetalRatesProto.Currency currency,
            MetalRatesProto.WeightUnit weightUnit
    ) {
        String json = restTemplate.getForObject(
                UriComponentsBuilder.fromUriString(baseUrl + "/spot-price")
                        .queryParam("metal", metal.name())
                        .queryParam("currency", currency.name())
                        .queryParam("weight_unit", weightUnit.name().toLowerCase())
                        .toUriString(),
                String.class
        );

        try {
            var builder = MetalRatesProto.SpotPrice.newBuilder();
            JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
            return builder.build();
        } catch (Exception e) {
           throw new BusinessException(
                    "RATE_FETCH_FAILED",
                    "failed to fetch live rate"
           );
        }
    }

    // ---------------- TODAY ----------------

    public MetalRatesProto.SpotPricesResponse fetchTodayRates(
            MetalRatesProto.Metal metal,
            MetalRatesProto.Currency currency,
            MetalRatesProto.WeightUnit weightUnit,
            Integer page,
            Integer limit
    ) {
        UriComponentsBuilder b = UriComponentsBuilder
                .fromUriString(baseUrl + "/spot-prices")
                .queryParam("metal", metal.name())
                .queryParam("currency", currency.name())
                .queryParam("weight_unit", weightUnit.name().toLowerCase());

        if (page != null) b.queryParam("page", page);
        if (limit != null) b.queryParam("limit", limit);

        String json = restTemplate.getForObject(b.toUriString(), String.class);

        try {
            var builder = MetalRatesProto.SpotPricesResponse.newBuilder();
            JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
            return builder.build();
        } catch (Exception e) {
            throw new BusinessException(
                    "DAILY_RATE_FETCH_FAILED",
                    "failed to fetch Daily rate"
            );
        }
    }

    // ---------------- HISTORICAL ----------------

    public MetalRatesProto.HistoricalSpotPricesResponse fetchHistoricalRates(
            MetalRatesProto.Metal metal,
            MetalRatesProto.Currency currency,
            MetalRatesProto.WeightUnit weightUnit,
            Integer page,
            Integer limit
    ) {
        UriComponentsBuilder b = UriComponentsBuilder
                .fromUriString(baseUrl + "/historical-spot-prices")
                .queryParam("metal", metal.name())
                .queryParam("currency", currency.name())
                .queryParam("weight_unit", weightUnit.name().toLowerCase());

        if (page != null) b.queryParam("page", page);
        if (limit != null) b.queryParam("limit", limit);

        String json = restTemplate.getForObject(b.toUriString(), String.class);

        try {
            var builder = MetalRatesProto.HistoricalSpotPricesResponse.newBuilder();
            JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
            return builder.build();
        } catch (Exception e) {
            throw new BusinessException(
                    "HISTORY_FETCH_FAILED",
                    "Failed to parse historical rates response"
            );
        }
    }
}
