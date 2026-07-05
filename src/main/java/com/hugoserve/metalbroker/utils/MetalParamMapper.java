package com.hugoserve.metalbroker.utils;

import com.hugoserve.metalbroker.proto.MetalRatesProto.Metal;

public final class MetalParamMapper {

    private MetalParamMapper() {}

    public static Metal fromApi(String apiMetal) {
        if (apiMetal == null || apiMetal.isBlank()) {
            return Metal.UNRECOGNIZED;
        }

        // Convert to lowercase and trim to ensure " gold " or "GOLD" works
        return switch (apiMetal.trim().toLowerCase()) {
            case "gold", "xau"       -> Metal.XAU;
            case "silver", "xag"     -> Metal.XAG;
            case "platinum", "xpt"   -> Metal.XPT;
            case "palladium", "xpd"  -> Metal.XPD;
            default                  -> Metal.UNRECOGNIZED;
        };
    }

    public static String toApi(Metal metal) {
        return switch (metal) {
            case XAU -> "XAU";
            case XAG -> "XAG";
            case XPT -> "XPT";
            case XPD -> "XPD";
            default  -> throw new IllegalArgumentException("Unknown metal enum: " + metal);
        };
    }

}
