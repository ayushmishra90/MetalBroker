package com.hugoserve.metalbroker.domain.dao.impl;

import com.hugoserve.metalbroker.domain.dao.WalletDAO;
import com.hugoserve.metalbroker.service.proto.wallet.WalletEntity;
import com.google.protobuf.Timestamp;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Repository
public class WalletJdbcDAO implements WalletDAO {

    private final JdbcTemplate jdbc;

    public WalletJdbcDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ---------------- PROTO MAPPER ----------------
    private static final RowMapper<WalletEntity> PROTO_RM = (rs, i) -> {
        Instant createdAt = rs.getTimestamp("created_at").toInstant();
        return WalletEntity.newBuilder()
                .setWalletId(rs.getLong("id"))
                .setUserId(rs.getLong("user_id"))
                .setInrBalance(rs.getBigDecimal("inr_balance").doubleValue())
                .setCreatedAtUtc(
                        Timestamp.newBuilder()
                                .setSeconds(createdAt.getEpochSecond())
                                .setNanos(createdAt.getNano())
                                .build()
                )
                .build();
    };
    @Override
    public WalletEntity createProto(long userId) {
        jdbc.update(
                "INSERT INTO wallets (user_id, inr_balance, created_at) VALUES (?, ?, ?)",
                userId,
                BigDecimal.ZERO,
                java.sql.Timestamp.from(Instant.now())
        );
        return findByUserIdProto(userId).orElseThrow();
    }

    @Override
    public void updateBalance(long walletId, BigDecimal balance) {
        jdbc.update(
                "UPDATE wallets SET inr_balance = ? WHERE id = ?",
                balance,
                walletId
        );
    }

    // ================= PROTO =================
    @Override
    public Optional<WalletEntity> findByUserIdProto(long userId) {
        return jdbc.query(
                "SELECT * FROM wallets WHERE user_id = ?",
                PROTO_RM,
                userId
        ).stream().findFirst();
    }
}
