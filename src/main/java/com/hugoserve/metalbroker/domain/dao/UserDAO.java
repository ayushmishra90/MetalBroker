package com.hugoserve.metalbroker.domain.dao;

import com.hugoserve.metalbroker.domain.db.UserDB;
import com.hugoserve.metalbroker.service.proto.auth.UserEntity;

import java.util.Optional;

public interface UserDAO {

    // ---------- DB ----------
    Optional<UserDB> findByEmail(String email);
    long insert(String email, String password, String role);
    void updateRefreshToken(long userId, String refreshToken);

    // ---------- PROTO ----------
    Optional<UserEntity> findByEmailProto(String email);
}
