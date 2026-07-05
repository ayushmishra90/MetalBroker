package com.hugoserve.metalbroker.service.impl;

import com.google.protobuf.Timestamp;
import com.hugoserve.metalbroker.facade.DbRateAnalysisFacade;
import com.hugoserve.metalbroker.proto.MetalRatesProto;
import com.hugoserve.metalbroker.service.DbRateAnalysisService;
import com.hugoserve.metalbroker.service.proto.metal.SpotTick5mEntity;
import com.hugoserve.metalbroker.utils.ProtoJson;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
@Service
@RequiredArgsConstructor
public class DbRateAnalysisServiceImpl implements DbRateAnalysisService {

    private final DbRateAnalysisFacade facade;

    @Override
    public String analyze(
            MetalRatesProto.Metal metal,
            Instant from,
            Instant to
    ) {

        validate(metal, from, to);

        // ✅ PROTO ENTITIES ONLY
        List<SpotTick5mEntity> points =
                facade.findIntraday(metal.name(), from, to);

        if (points.size() < 2) {
            return failure("Not enough data points");
        }

        double start = points.getFirst().getMid();
        double end   = points.getLast().getMid();

        double high = points.stream()
                .mapToDouble(SpotTick5mEntity::getMid)
                .max()
                .orElse(start);

        double low = points.stream()
                .mapToDouble(SpotTick5mEntity::getMid)
                .min()
                .orElse(start);

        double absChange = end - start;
        double pctChange = (absChange / start) * 100.0;
        double volatility = ((high - low) / start) * 100.0;

        String trend = resolveTrend(pctChange);

        return success(
                metal,
                from,
                to,
                start,
                end,
                high,
                low,
                absChange,
                pctChange,
                volatility,
                trend
        );
    }

    // ---------------- HELPERS ----------------

    private void validate(
            MetalRatesProto.Metal metal,
            Instant from,
            Instant to
    ) {
        if (metal == MetalRatesProto.Metal.METAL_UNSPECIFIED) {
            throw new IllegalArgumentException("metal is required");
        }
        if (from == null || to == null) {
            throw new IllegalArgumentException("from/to required");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from must be <= to");
        }
    }

    private String resolveTrend(double pct) {
        if (pct > 0.05) return "UP";
        if (pct < -0.05) return "DOWN";
        return "FLAT";
    }

    private String success(
            MetalRatesProto.Metal metal,
            Instant from,
            Instant to,
            double start,
            double end,
            double high,
            double low,
            double absChange,
            double pctChange,
            double volatility,
            String trend
    ) {

        return ProtoJson.print(
                MetalRatesProto.IntradayAnalysisResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage("Analysis computed")
                        .setMetal(metal)
                        .setFromUtc(ts(from))
                        .setToUtc(ts(to))
                        .setAnalysis(
                                MetalRatesProto.RateAnalysis.newBuilder()
                                        .setStartPrice(start)
                                        .setEndPrice(end)
                                        .setHigh(high)
                                        .setLow(low)
                                        .setAbsoluteChange(absChange)
                                        .setPercentageChange(pctChange)
                                        .setVolatilityPct(volatility)
                                        .setTrend(trend)
                                        .build()
                        )
                        .build()
        );
    }

    private String failure(String msg) {
        return ProtoJson.print(
                MetalRatesProto.IntradayAnalysisResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage(msg)
                        .build()
        );
    }

    private Timestamp ts(Instant i) {
        return Timestamp.newBuilder()
                .setSeconds(i.getEpochSecond())
                .setNanos(i.getNano())
                .build();
    }
}
