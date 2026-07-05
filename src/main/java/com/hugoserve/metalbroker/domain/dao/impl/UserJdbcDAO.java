package com.hugoserve.metalbroker.domain.dao.impl;

import com.hugoserve.metalbroker.domain.dao.UserDAO;
import com.hugoserve.metalbroker.domain.db.UserDB;
import com.hugoserve.metalbroker.service.proto.auth.UserEntity;
import com.hugoserve.metalbroker.service.proto.auth.UserRole;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserJdbcDAO implements UserDAO {

    private final JdbcTemplate jdbc;

    public UserJdbcDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ---------------- DB MAPPER ----------------
    private static final RowMapper<UserDB> DB_RM = (rs, i) -> {
        UserDB u = new UserDB();
        u.setId(rs.getLong("id"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setCurrentRefreshToken(rs.getString("current_refresh_token"));
        u.setRole(
                com.hugoserve.metalbroker.proto.MetalRatesProto.UserRole
                        .valueOf(rs.getString("role"))
        );
        return u;
    };

    // ---------------- PROTO MAPPER ----------------
    private static final RowMapper<UserEntity> PROTO_RM = (rs, i) ->
            UserEntity.newBuilder()
                    .setId(rs.getLong("id"))
                    .setEmail(rs.getString("email"))
                    .setRole(UserRole.valueOf(rs.getString("role")))
                    .build();

    // ================= DB =================
    @Override
    public Optional<UserDB> findByEmail(String email) {
        return jdbc.query(
                "SELECT * FROM users WHERE email = ?",
                DB_RM,
                email
        ).stream().findFirst();
    }

    @Override
    public long insert(String email, String password, String role) {
        jdbc.update(
                "INSERT INTO users(email, password, role) VALUES (?, ?, ?)",
                email,
                password,
                role
        );
        return jdbc.queryForObject(
                "SELECT id FROM users WHERE email = ?",
                Long.class,
                email
        );
    }

    @Override
    public void updateRefreshToken(long userId, String refreshToken) {
        jdbc.update(
                "UPDATE users SET current_refresh_token = ? WHERE id = ?",
                refreshToken,
                userId
        );
    }

    // ================= PROTO =================
    @Override
    public Optional<UserEntity> findByEmailProto(String email) {
        return jdbc.query(
                "SELECT id, email, role FROM users WHERE email = ?",
                PROTO_RM,
                email
        ).stream().findFirst();
    }
}
