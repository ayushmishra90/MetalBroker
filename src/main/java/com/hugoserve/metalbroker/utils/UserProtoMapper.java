package com.hugoserve.metalbroker.utils;

import com.hugoserve.metalbroker.domain.db.UserDB;
import com.hugoserve.metalbroker.service.proto.auth.UserEntity;

public final class UserProtoMapper {

    private UserProtoMapper() {}

    public static UserEntity toProto(UserDB db) {
        return UserEntity.newBuilder()
                .setId(db.getId())
                .setEmail(db.getEmail())
                .setRole(
                        com.hugoserve.metalbroker.service.proto.auth.UserRole
                                .valueOf(db.getRole().name())
                )
                .build();
    }
}
