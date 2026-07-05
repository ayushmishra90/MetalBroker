package com.hugoserve.metalbroker.utils;

import com.hugoserve.metalbroker.proto.common.ApiError;

public final class ResponseErrorBuilder {

    private ResponseErrorBuilder() {}

    public static String error(String code, String message) {
        return ProtoJson.print(
                ApiError.newBuilder()
                        .setSuccess(false)
                        .setCode(code)
                        .setMessage(message)
                        .build()
        );
    }
}
