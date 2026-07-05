package com.hugoserve.metalbroker.feed;

import com.google.protobuf.util.JsonFormat;
import com.hugoserve.metalbroker.proto.MetalRatesProto;
import com.hugoserve.metalbroker.proto.MetalRatesProto.Metal;
import com.hugoserve.metalbroker.utils.MetalParamMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class GoldBrokerApiClientImpl implements GoldBrokerApiClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public GoldBrokerApiClientImpl(RestTemplate restTemplate,
                                   @Value("${metalbroker.feed.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    private String resolveApiSymbol(String metalNameOrCode) {
        // If user passes "gold" map -> XAU, else if passes "XAU" keep it.
        // Your MetalParamMapper currently maps names; keep your behavior:
        Metal metalEnum = MetalParamMapper.fromApi(metalNameOrCode);

        if (metalEnum == Metal.UNRECOGNIZED || metalEnum == Metal.METAL_UNSPECIFIED) {
            throw new IllegalArgumentException("Unsupported metal: " + metalNameOrCode);
        }
        return MetalParamMapper.toApi(metalEnum); // "XAU"
    }

    private String buildUrl(String path, String metalCode, String currency, String unit) {
        String apiSymbol = resolveApiSymbol(metalCode);
        return UriComponentsBuilder.fromUriString(baseUrl + path)
                .queryParam("metal", apiSymbol)
                .queryParam("currency", currency)
                .queryParam("weight_unit", unit)
                .toUriString();
    }

    private <T extends com.google.protobuf.Message.Builder> Object parseJson(String json, T builder) {
        try {
            JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse API response into proto", e);
        }
    }

    @Override
    public MetalRatesProto.SpotPrice fetchCurrent(String metalCode, String currency, String unit) {
        String url = buildUrl("/spot-price", metalCode, currency, unit);
        String json = restTemplate.getForObject(url, String.class);
        return (MetalRatesProto.SpotPrice) parseJson(json, MetalRatesProto.SpotPrice.newBuilder());
    }

    @Override
    public MetalRatesProto.SpotPricesResponse fetchIntraday(String metalCode, String currency, String unit) {
        String url = buildUrl("/spot-prices", metalCode, currency, unit);
        String json = restTemplate.getForObject(url, String.class);
        return (MetalRatesProto.SpotPricesResponse) parseJson(json, MetalRatesProto.SpotPricesResponse.newBuilder());
    }

    @Override
    public MetalRatesProto.HistoricalSpotPricesResponse fetchDailyHistory(String metalCode, String currency, String unit) {
        String url = buildUrl("/historical-spot-prices", metalCode, currency, unit);
        String json = restTemplate.getForObject(url, String.class);
        return (MetalRatesProto.HistoricalSpotPricesResponse) parseJson(json, MetalRatesProto.HistoricalSpotPricesResponse.newBuilder());
    }
}
