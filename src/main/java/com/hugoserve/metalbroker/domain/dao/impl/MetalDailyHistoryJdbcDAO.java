package com.hugoserve.metalbroker.domain.dao.impl;

import com.hugoserve.metalbroker.domain.dao.MetalDailyHistoryDAO;
import com.hugoserve.metalbroker.proto.MetalRatesProto;
import com.hugoserve.metalbroker.service.proto.metal.MetalDailyHistoryProto.MetalDailyHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class MetalDailyHistoryJdbcDAO implements MetalDailyHistoryDAO {

    private final JdbcTemplate jdbc;

    private static final RowMapper<MetalDailyHistoryEntity> PROTO_RM = (rs, i) -> {
        var b = MetalDailyHistoryEntity.newBuilder()
                .setId(rs.getLong("id"))
                .setMetalId(rs.getLong("metal_id"))
                .setDayUtc(rs.getDate("day_utc").toLocalDate().toString())
                .setOpen(rs.getBigDecimal("open").doubleValue())
                .setHigh(rs.getBigDecimal("high").doubleValue())
                .setLow(rs.getBigDecimal("low").doubleValue())
                .setClose(rs.getBigDecimal("close").doubleValue())
                .setSource(rs.getString("source"));

        if (rs.getBigDecimal("ma50") != null) {
            b.setMa50(rs.getBigDecimal("ma50").doubleValue());
        }
        if (rs.getBigDecimal("ma200") != null) {
            b.setMa200(rs.getBigDecimal("ma200").doubleValue());
        }

        return b.build();
    };

    public MetalDailyHistoryJdbcDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }
    @Override
    public void upsertProto(long metalId, MetalRatesProto.DailyHistoryCandle c) {
        jdbc.update("""
        INSERT INTO metal_daily_history
        (metal_id, day_utc, open, high, low, close, ma50, ma200, source)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE
            open = VALUES(open),
            high = VALUES(high),
            low = VALUES(low),
            close = VALUES(close),
            ma50 = VALUES(ma50),
            ma200 = VALUES(ma200),
            source = VALUES(source)
        """,
                metalId,
                LocalDate.parse(c.getDayUtc()),
                c.getOpen(),
                c.getHigh(),
                c.getLow(),
                c.getClose(),
                c.hasMa50() ? c.getMa50() : null,
                c.hasMa200() ? c.getMa200() : null,
                "goldbroker"
        );
    }

    @Override
    public int deleteByMetalAndDay(long metalId, LocalDate dayUtc) {
        return jdbc.update(
                "DELETE FROM metal_daily_history WHERE metal_id = ? AND day_utc = ?",
                metalId, dayUtc
        );
    }

    @Override
    public int deleteByMetalAndRange(long metalId, LocalDate from, LocalDate to) {
        return jdbc.update(
                "DELETE FROM metal_daily_history WHERE metal_id = ? AND day_utc BETWEEN ? AND ?",
                metalId, from, to
        );
    }
    @Override
    public LocalDate findMaxDayUtc(long metalId) {
        return jdbc.queryForList("""
        SELECT MAX(day_utc)
        FROM metal_daily_history
        WHERE metal_id = ?
        """, LocalDate.class, metalId
        ).stream().findFirst().orElse(null);
    }


    @Override
    public List<MetalDailyHistoryEntity> findBetweenProto(
            long metalId,
            LocalDate from,
            LocalDate to
    ) {
        return jdbc.query(
                """
                SELECT * FROM metal_daily_history
                WHERE metal_id = ?
                  AND day_utc BETWEEN ? AND ?
                ORDER BY day_utc ASC
                """,
                PROTO_RM,
                metalId, from, to
        );
    }

    @Override
    public Page<MetalDailyHistoryEntity> findBetweenProtoPage(
            long metalId,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    ) {
        List<MetalDailyHistoryEntity> rows = jdbc.query(
                """
                SELECT * FROM metal_daily_history
                WHERE metal_id = ?
                  AND day_utc BETWEEN ? AND ?
                ORDER BY day_utc ASC
                LIMIT ? OFFSET ?
                """,
                PROTO_RM,
                metalId, from, to,
                pageable.getPageSize(),
                pageable.getOffset()
        );

        Long total = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM metal_daily_history
                WHERE metal_id = ?
                  AND day_utc BETWEEN ? AND ?
                """,
                Long.class,
                metalId, from, to
        );

        return new PageImpl<>(rows, pageable, total == null ? 0 : total);
    }



}