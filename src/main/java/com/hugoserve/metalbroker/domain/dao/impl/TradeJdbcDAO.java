package com.hugoserve.metalbroker.domain.dao.impl;

import com.hugoserve.metalbroker.domain.dao.TradeDAO;
import com.hugoserve.metalbroker.service.proto.trade.TradeEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public class TradeJdbcDAO implements TradeDAO {

    private final JdbcTemplate jdbc;

    public TradeJdbcDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }


    // =========================
    // PROTO ROW MAPPER
    // =========================
    private static final RowMapper<TradeEntity> PROTO_RM = (rs, i) ->
            TradeEntity.newBuilder()
                    .setId(rs.getLong("id"))
                    .setUserId(rs.getLong("user_id"))
                    .setType(rs.getString("type"))
                    .setMetal(rs.getString("metal"))
                    .setQuantity(rs.getBigDecimal("quantity").doubleValue())
                    .setExecutedPrice(rs.getBigDecimal("executed_price").doubleValue())
                    .setTotalAmount(rs.getBigDecimal("total_amount").doubleValue())
                    .setExecutedAtUtc(
                            com.google.protobuf.Timestamp.newBuilder()
                                    .setSeconds(rs.getTimestamp("executed_at").toInstant().getEpochSecond())
                                    .setNanos(rs.getTimestamp("executed_at").toInstant().getNano())
                                    .build()
                    )
                    .build();

    // =========================
    // PROTO METHODS
    // =========================
    @Override
    public TradeEntity insertProto(
            long userId,
            String type,
            String metal,
            double quantity,
            double executedPrice,
            double totalAmount
    ) {
        jdbc.update("""
                INSERT INTO trades
                (user_id, type, metal, quantity, executed_price, total_amount, executed_at)
                VALUES (?, ?, ?, ?, ?, ?, NOW())
                """,
                userId,
                type,
                metal,
                quantity,
                executedPrice,
                totalAmount
        );

        return jdbc.query("""
                SELECT *
                FROM trades
                WHERE user_id = ?
                ORDER BY id DESC
                LIMIT 1
                """,
                PROTO_RM,
                userId
        ).getFirst();
    }

    @Override
    public List<TradeEntity> findByUserProto(long userId) {
        return jdbc.query("""
                SELECT *
                FROM trades
                WHERE user_id = ?
                ORDER BY executed_at DESC
                """,
                PROTO_RM,
                userId
        );
    }
}
