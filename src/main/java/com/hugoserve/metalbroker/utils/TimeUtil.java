package com.hugoserve.metalbroker.utils;

import com.hugoserve.metalbroker.proto.MetalRatesProto;
import java.time.*;

public final class TimeUtil {

    public static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private TimeUtil() {}

    /** API UTC → IST LocalDateTime (wall clock) */
    public static LocalDateTime apiUtcToIstLocal(MetalRatesProto.SpotPrice p) {
        return Instant.ofEpochSecond(
                        p.getDate().getSeconds(),
                        p.getDate().getNanos()
                ).atZone(ZoneOffset.UTC)
                .withZoneSameInstant(IST)
                .toLocalDateTime();
    }

    /** Now in IST (wall clock) */
    public static LocalDateTime nowIstLocal() {
        return LocalDateTime.now(IST);
    }
}
