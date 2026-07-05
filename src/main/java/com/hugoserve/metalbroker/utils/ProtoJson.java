package com.hugoserve.metalbroker.utils;

import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;

public class ProtoJson {
    private static final JsonFormat.Printer PRINTER = JsonFormat.printer()
            .includingDefaultValueFields()
            .preservingProtoFieldNames();

    public static String print(Message msg) {
        try {
            return PRINTER.print(msg);
        } catch (Exception e) {
            throw new RuntimeException("Failed to print proto as JSON", e);
        }
    }
}
