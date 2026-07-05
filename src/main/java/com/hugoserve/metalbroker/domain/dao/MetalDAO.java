package com.hugoserve.metalbroker.domain.dao;

import com.hugoserve.metalbroker.service.proto.metal.MetalEntity;

import java.util.List;
import java.util.Optional;

public interface MetalDAO {

    // ---------- READ ----------
    Optional<MetalEntity> findByCodeProto(String code);
    List<MetalEntity> findAllProto();

    // ---------- WRITE ----------
    void upsert(String code, String name, boolean active);
    void update(String code, String name, Boolean active);

    Optional<Long> findIdByCode(String code);

}

