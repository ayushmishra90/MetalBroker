package com.hugoserve.metalbroker.domain.dao.impl;

import com.hugoserve.metalbroker.domain.dao.WalletMetalDAO;
import com.hugoserve.metalbroker.service.proto.wallet.WalletMetalEntity;
import com.google.protobuf.Timestamp;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class WalletMetalJdbcDAO implements WalletMetalDAO {

    private final JdbcTemplate jdbc;

    public WalletMetalJdbcDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }
    // ---------------- PROTO MAPPER ----------------
    private static final RowMapper<WalletMetalEntity> PROTO_RM = (rs, i) -> {
        Instant createdAt = rs.getTimestamp("created_at").toInstant();
        return WalletMetalEntity.newBuilder()
                .setId(rs.getLong("id"))
                .setWalletId(rs.getLong("wallet_id"))
                .setMetal(rs.getString("metal"))
                .setQuantity(rs.getBigDecimal("quantity").doubleValue())
                .setCreatedAtUtc(
                        Timestamp.newBuilder()
                                .setSeconds(createdAt.getEpochSecond())
                                .setNanos(createdAt.getNano())
                                .build()
                )
                .build();
    };

    // ================= PROTO =================
    @Override
    public Optional<WalletMetalEntity> findProto(long walletId, String metal) {
        return jdbc.query(
                "SELECT * FROM wallet_balances WHERE wallet_id = ? AND metal = ?",
                PROTO_RM,
                walletId,
                metal
        ).stream().findFirst();
    }

    @Override
    public List<WalletMetalEntity> findAllByWalletProto(long walletId) {
        return jdbc.query(
                "SELECT * FROM wallet_balances WHERE wallet_id = ?",
                PROTO_RM,
                walletId
        );
    }

    @Override
    public Optional<Double> findQuantity(long walletId, String metal) {
        return jdbc.query(
                "SELECT quantity FROM wallet_balances WHERE wallet_id = ? AND metal = ?",
                (rs, i) -> rs.getDouble("quantity"),
                walletId,
                metal
        ).stream().findFirst();
    }

}
