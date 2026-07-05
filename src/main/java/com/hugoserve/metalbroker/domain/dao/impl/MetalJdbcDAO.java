package com.hugoserve.metalbroker.domain.dao.impl;

import com.hugoserve.metalbroker.domain.dao.MetalDAO;
import com.hugoserve.metalbroker.service.proto.metal.MetalEntity;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public class MetalJdbcDAO implements MetalDAO {

    private final JdbcTemplate jdbc;

    public MetalJdbcDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ================= READ =================

    @Override
    public Optional<MetalEntity> findByCodeProto(String code) {
        return jdbc.query(
                """
                SELECT id, code, name, is_active
                FROM metals
                WHERE UPPER(code) = ?
                """,
                PROTO_RM,
                code.toUpperCase()
        ).stream().findFirst();
    }

    @Override
    public List<MetalEntity> findAllProto() {
        return jdbc.query(
                """
                SELECT id, code, name, is_active
                FROM metals
                ORDER BY code
                """,
                PROTO_RM
        );
    }

    // ================= WRITE =================

    @Override
    public void upsert(String code, String name, boolean active) {
        jdbc.update(
                """
                INSERT INTO metals (code, name, is_active)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE
                  name = VALUES(name),
                  is_active = VALUES(is_active)
                """,
                code, name, active
        );
    }

    @Override
    public void update(String code, String name, Boolean active) {

        if (name != null) {
            jdbc.update(
                    "UPDATE metals SET name = ? WHERE code = ?",
                    name, code
            );
        }

        if (active != null) {
            jdbc.update(
                    "UPDATE metals SET is_active = ? WHERE code = ?",
                    active, code
            );
        }
    }
    @Override
    public Optional<Long> findIdByCode(String code) {
        return jdbc.query(
                "SELECT id FROM metals WHERE UPPER(code) = ?",
                (rs, i) -> rs.getLong("id"),
                code.toUpperCase()
        ).stream().findFirst();
    }

    // ================= MAPPER =================

    private static final RowMapper<MetalEntity> PROTO_RM =
            (rs, i) -> MetalEntity.newBuilder()
                    .setId(rs.getLong("id"))
                    .setCode(rs.getString("code"))
                    .setName(rs.getString("name"))
                    .setActive(rs.getBoolean("is_active"))
                    .build();
}
