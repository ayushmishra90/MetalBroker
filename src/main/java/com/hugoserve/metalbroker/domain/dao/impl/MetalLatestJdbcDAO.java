package com.hugoserve.metalbroker.domain.dao.impl;

import com.hugoserve.metalbroker.domain.dao.MetalLatestDAO;
import com.hugoserve.metalbroker.proto.MetalRatesProto;
import com.hugoserve.metalbroker.service.proto.metal.MetalLatestEntity;
import com.hugoserve.metalbroker.utils.JdbcTimeUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class MetalLatestJdbcDAO implements MetalLatestDAO {

    private final JdbcTemplate jdbc;

    public MetalLatestJdbcDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<MetalLatestEntity> PROTO_RM = (rs, i) ->
            MetalLatestEntity.newBuilder()
                    .setMetalId(rs.getLong("metal_id"))
                    .setMetalCode(rs.getString("code"))
                    .setFeedTsUtc(
                            JdbcTimeUtil.getInstantUtc(rs, "feed_ts_utc") == null
                                    ? null
                                    : com.google.protobuf.Timestamp.newBuilder()
                                    .setSeconds(JdbcTimeUtil.getInstantUtc(rs, "feed_ts_utc").getEpochSecond())
                                    .setNanos(JdbcTimeUtil.getInstantUtc(rs, "feed_ts_utc").getNano())
                                    .build()
                    )
                    .setCapturedAtUtc(
                            JdbcTimeUtil.getInstantUtc(rs, "captured_at_utc") == null
                                    ? null
                                    : com.google.protobuf.Timestamp.newBuilder()
                                    .setSeconds(JdbcTimeUtil.getInstantUtc(rs, "captured_at_utc").getEpochSecond())
                                    .setNanos(JdbcTimeUtil.getInstantUtc(rs, "captured_at_utc").getNano())
                                    .build()
                    )
                    .setAsk(rs.getBigDecimal("ask").doubleValue())
                    .setMid(rs.getBigDecimal("mid").doubleValue())
                    .setBid(rs.getBigDecimal("bid").doubleValue())
                    .setValue(rs.getBigDecimal("value").doubleValue())
                    .setPerformance(rs.getBigDecimal("performance").doubleValue())
                    .setSource(rs.getString("source"))
                    .build();


    @Override
    public Optional<Instant> findFeedTsUtcByMetalCode(String metalCode) {
        return jdbc.query("""
                SELECT ml.feed_ts_utc
                FROM metal_latest ml
                JOIN metals m ON m.id = ml.metal_id
                WHERE m.code = ?
                """,
                (rs, i) -> JdbcTimeUtil.getInstantUtc(rs, "feed_ts_utc"),
                metalCode.toUpperCase()
        ).stream().findFirst();
    }

    @Override
    public Optional<Instant> findCapturedAtUtcByMetalCode(String metalCode) {
        return jdbc.query("""
                SELECT ml.captured_at_utc
                FROM metal_latest ml
                JOIN metals m ON m.id = ml.metal_id
                WHERE m.code = ?
                """,
                (rs, i) -> JdbcTimeUtil.getInstantUtc(rs, "captured_at_utc"),
                metalCode.toUpperCase()
        ).stream().findFirst();
    }
    @Override
    public void upsertProto(long metalId, MetalRatesProto.SpotPrice p) {

        Instant feedTs = Instant.ofEpochSecond(
                p.getDate().getSeconds(),
                p.getDate().getNanos()
        );

        Instant capturedAt = Instant.now();

        jdbc.update("""
        INSERT INTO metal_latest
        (metal_id, feed_ts_utc, captured_at_utc, ask, mid, bid, value, performance, source)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE
            feed_ts_utc = VALUES(feed_ts_utc),
            captured_at_utc = VALUES(captured_at_utc),
            ask = VALUES(ask),
            mid = VALUES(mid),
            bid = VALUES(bid),
            value = VALUES(value),
            performance = VALUES(performance),
            source = VALUES(source)
        """,
                metalId,
                feedTs,
                capturedAt,
                p.getAsk(),
                p.getMid(),
                p.getBid(),
                p.getValue(),
                p.getPerformance(),
                "goldbroker"
        );
    }



//    proto

    @Override
    public Optional<MetalLatestEntity> findByMetalCodeProto(String metalCode) {
        return jdbc.query(
                """
                SELECT ml.*, m.code
                FROM metal_latest ml
                JOIN metals m ON m.id = ml.metal_id
                WHERE m.code = ?
                """,
                PROTO_RM,
                metalCode.toUpperCase()
        ).stream().findFirst();
    }

    @Override
    public List<MetalLatestEntity> findAllProto() {
        return jdbc.query(
                """
                SELECT ml.*, m.code
                FROM metal_latest ml
                JOIN metals m ON m.id = ml.metal_id
                """,
                PROTO_RM
        );
    }

}
