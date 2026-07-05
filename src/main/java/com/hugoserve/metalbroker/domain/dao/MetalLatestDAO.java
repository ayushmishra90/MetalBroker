package com.hugoserve.metalbroker.domain.dao;

import com.hugoserve.metalbroker.proto.MetalRatesProto;
import com.hugoserve.metalbroker.service.proto.metal.MetalLatestEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MetalLatestDAO {

    Optional<Instant> findFeedTsUtcByMetalCode(String metalCode);
    Optional<Instant> findCapturedAtUtcByMetalCode(String metalCode);


    void upsertProto(long metalId, MetalRatesProto.SpotPrice price);


    // ---------- PROTO ----------
    Optional<MetalLatestEntity> findByMetalCodeProto(String metalCode);
    List<MetalLatestEntity> findAllProto();

}
