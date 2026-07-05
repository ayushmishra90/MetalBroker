package com.hugoserve.metalbroker.utils;

import com.google.protobuf.Timestamp;
import com.hugoserve.metalbroker.proto.MetalRatesProto;
import com.hugoserve.metalbroker.service.proto.metal.MetalDailyHistoryProto;
import com.hugoserve.metalbroker.service.proto.metal.MetalEntity;
import com.hugoserve.metalbroker.service.proto.metal.MetalLatestEntity;
import com.hugoserve.metalbroker.service.proto.metal.SpotTick5mEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DbProtoMapper {

    public static MetalRatesProto.RegisterRequest parseRegister(String body) {
        return ProtoJsonParser.parse(body, MetalRatesProto.RegisterRequest.newBuilder()).build();
    }
    public MetalRatesProto.Metal toMetalEnum(String code) {
        if (code == null) return MetalRatesProto.Metal.METAL_UNSPECIFIED;
        try {
            return MetalRatesProto.Metal.valueOf(code.trim().toUpperCase()); // "XAU"
        } catch (Exception e) {
            return MetalRatesProto.Metal.METAL_UNSPECIFIED;
        }
    }

    // =====================================================
    // LATEST (PROTO → PROTO)
    // =====================================================
    public MetalRatesProto.LatestSnapshot latestSnapshotFromProto(
            MetalLatestEntity e
    ) {
        return MetalRatesProto.LatestSnapshot.newBuilder()
                .setMetal(toMetalEnum(e.getMetalCode()))
                .setFeedTsUtc(e.getFeedTsUtc())
                .setCapturedAtUtc(e.getCapturedAtUtc())
                .setAsk(e.getAsk())
                .setMid(e.getMid())
                .setBid(e.getBid())
                .setValue(e.getValue())
                .setPerformance(e.getPerformance())
                .setSource(e.getSource())
                .build();
    }

    // =====================================================
    // TICK 5m (PROTO → PROTO)
    // =====================================================
    public MetalRatesProto.Tick5m tick5mFromProto(
            String metalCode,
            SpotTick5mEntity e
    ) {
        return MetalRatesProto.Tick5m.newBuilder()
                .setMetal(toMetalEnum(metalCode))
                .setTsUtc(e.getTsUtc())
                .setAsk(e.getAsk())
                .setMid(e.getMid())
                .setBid(e.getBid())
                .setValue(e.getValue())
                .setPerformance(e.getPerformance())
                .setSource(e.getSource())
                .build();
    }

    // =========================
    // Tick5m → SpotPrice
    // =========================
    public static MetalRatesProto.SpotPrice toSpotPrice(
            MetalRatesProto.Tick5m tick
    ) {
        return MetalRatesProto.SpotPrice.newBuilder()
                .setDate(
                        Timestamp.newBuilder()
                                .setSeconds(tick.getTsUtc().getSeconds())
                                .setNanos(tick.getTsUtc().getNanos())
                                .build()
                )
                .setAsk(tick.getAsk())
                .setMid(tick.getMid())
                .setBid(tick.getBid())
                .setValue(tick.getValue())
                .setPerformance(tick.getPerformance())
                .build();
    }

    // =====================================================
    // DAILY HISTORY (PROTO → PROTO)
    // =====================================================
    public MetalRatesProto.DailyHistoryCandle dailyHistoryCandleFromProto(
            String metalCode,
            MetalDailyHistoryProto.MetalDailyHistoryEntity e
    ) {
        var b = MetalRatesProto.DailyHistoryCandle.newBuilder()
                .setMetal(toMetalEnum(metalCode))
                .setDayUtc(e.getDayUtc())
                .setOpen(e.getOpen())
                .setHigh(e.getHigh())
                .setLow(e.getLow())
                .setClose(e.getClose())
                .setSource(e.getSource());

        if (e.hasMa50()) b.setMa50(e.getMa50());
        if (e.hasMa200()) b.setMa200(e.getMa200());

        return b.build();
    }

    public Timestamp toTs(Instant instant) {
        if (instant == null) return Timestamp.getDefaultInstance();
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

    public MetalRatesProto.MetalInfo metalInfo(MetalEntity e) {
        return MetalRatesProto.MetalInfo.newBuilder()
                .setId(e.getId())
                .setMetal(MetalRatesProto.Metal.valueOf(e.getCode()))
                .setName(e.getName())
                .setIsActive(e.getActive())
                .build();
    }

}
