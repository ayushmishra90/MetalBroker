package com.hugoserve.metalbroker.utils;

import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;

public final class ProtoJsonParser {
    private ProtoJsonParser() {}

    public static <T extends Message.Builder> T parse(String json, T builder) {
        try {
            JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
            return builder;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse proto JSON", e);
        }
    }
}
