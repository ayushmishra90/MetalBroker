package com.hugoserve.metalbroker.domain.dao.impl;

import com.google.protobuf.Timestamp;
import com.hugoserve.metalbroker.domain.dao.SpotTick5mDAO;
import com.hugoserve.metalbroker.proto.MetalRatesProto;
import com.hugoserve.metalbroker.service.proto.metal.SpotTick5mEntity;
import com.hugoserve.metalbroker.utils.JdbcTimeUtil;
import org.springframework.data.domain.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class SpotTick5mJdbcDAO implements SpotTick5mDAO {

    private final JdbcTemplate jdbc;

    public SpotTick5mJdbcDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }
    private static final RowMapper<SpotTick5mEntity> PROTO_RM = (rs, i) ->
            SpotTick5mEntity.newBuilder()
                    .setMetalId(rs.getLong("metal_id"))
                    .setTsUtc(
                            Timestamp.newBuilder()
                                    .setSeconds(
                                            JdbcTimeUtil.getInstantUtc(rs, "ts_utc").getEpochSecond()
                                    )
                                    .setNanos(
                                            JdbcTimeUtil.getInstantUtc(rs, "ts_utc").getNano()
                                    )
                                    .build()
                    )
                    .setAsk(rs.getBigDecimal("ask").doubleValue())
                    .setMid(rs.getBigDecimal("mid").doubleValue())
                    .setBid(rs.getBigDecimal("bid").doubleValue())
                    .setValue(rs.getBigDecimal("value").doubleValue())
                    .setPerformance(rs.getBigDecimal("performance").doubleValue())
                    .setSource(rs.getString("source"))
                    .build();

    // ---------- UPSERT ----------

    public void upsertProtoBatch(long metalId, List<MetalRatesProto.SpotPrice> prices) {

        jdbc.batchUpdate("""
        INSERT INTO spot_ticks_5m
        (metal_id, ts_utc, ask, mid, bid, value, performance, source)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE
            ask = VALUES(ask),
            mid = VALUES(mid),
            bid = VALUES(bid),
            value = VALUES(value),
            performance = VALUES(performance),
            source = VALUES(source)
    """,
                prices,
                50,
                (ps, p) -> {
                    Instant ts = Instant.ofEpochSecond(
                            p.getDate().getSeconds(),
                            p.getDate().getNanos()
                    );

                    ps.setLong(1, metalId);
                    ps.setTimestamp(2, java.sql.Timestamp.from(ts)); // OK (Instant supported)
                    ps.setDouble(3, p.getAsk());
                    ps.setDouble(4, p.getMid());
                    ps.setDouble(5, p.getBid());
                    ps.setDouble(6, p.getValue());
                    ps.setDouble(7, p.getPerformance());
                    ps.setString(8, "goldbroker");
                }
        );
    }

    @Override
    public void upsertProto(long metalId, MetalRatesProto.SpotPrice p) {
        Instant ts = Instant.ofEpochSecond(
                p.getDate().getSeconds(),
                p.getDate().getNanos()
        );

        jdbc.update("""
        INSERT INTO spot_ticks_5m
        (metal_id, ts_utc, ask, mid, bid, value, performance, source)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE
            ask = VALUES(ask),
            mid = VALUES(mid),
            bid = VALUES(bid),
            value = VALUES(value),
            performance = VALUES(performance),
            source = VALUES(source)
        """,
                metalId,
                ts,
                p.getAsk(),
                p.getMid(),
                p.getBid(),
                p.getValue(),
                p.getPerformance(),
                "goldbroker"
        );
    }


    // ---------- DELETE ----------
    @Override
    public int deleteById(long id) {
        return jdbc.update("DELETE FROM spot_ticks_5m WHERE id = ?", id);
    }

    @Override
    public int deleteByMetalAndRange(long metalId, Instant from, Instant to) {
        return jdbc.update("""
                DELETE FROM spot_ticks_5m
                WHERE metal_id = ?
                  AND ts_utc BETWEEN ? AND ?
                """,
                metalId, from, to
        );
    }

    @Override
    public long deleteBefore(Instant cutoff) {
        return jdbc.update("""
                DELETE FROM spot_ticks_5m
                WHERE ts_utc <= ?
                """,
                cutoff
        );
    }



    @Override
    public Optional<Instant> findLastTs(long metalId) {
        return jdbc.query("""
                SELECT MAX(ts_utc) AS ts_utc
                FROM spot_ticks_5m
                WHERE metal_id = ?
                """,
                (rs, i) -> JdbcTimeUtil.getInstantUtc(rs, "ts_utc"),
                metalId
        ).stream().findFirst();
    }


    @Override
    public List<SpotTick5mEntity> findByMetalCodeProto(
            String metalCode, Instant from, Instant to) {

        return jdbc.query("""
        SELECT t.*, m.code
        FROM spot_ticks_5m t
        JOIN metals m ON m.id = t.metal_id
        WHERE m.code = ?
          AND t.ts_utc BETWEEN ? AND ?
        ORDER BY t.ts_utc ASC
    """, PROTO_RM, metalCode.toUpperCase(), from, to);
    }

    @Override
    public Page<SpotTick5mEntity> findIntradayProtoPage(
            long metalId, Instant from, Instant to, Pageable pageable) {

        List<SpotTick5mEntity> rows = jdbc.query("""
        SELECT t.*, m.code
        FROM spot_ticks_5m t
        JOIN metals m ON m.id = t.metal_id
        WHERE t.metal_id = ?
          AND t.ts_utc BETWEEN ? AND ?
        ORDER BY t.ts_utc ASC
        LIMIT ? OFFSET ?
    """, PROTO_RM,
                metalId, from, to,
                pageable.getPageSize(), pageable.getOffset()
        );

        Long total = jdbc.queryForObject("""
        SELECT COUNT(*)
        FROM spot_ticks_5m
        WHERE metal_id = ?
          AND ts_utc BETWEEN ? AND ?
    """, Long.class, metalId, from, to);

        return new PageImpl<>(rows, pageable, total == null ? 0 : total);
    }


}
