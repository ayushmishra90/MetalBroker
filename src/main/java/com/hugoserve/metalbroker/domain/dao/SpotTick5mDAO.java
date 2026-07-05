package com.hugoserve.metalbroker.domain.dao;

import com.hugoserve.metalbroker.proto.MetalRatesProto;
import com.hugoserve.metalbroker.service.proto.metal.SpotTick5mEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SpotTick5mDAO {

    Optional<Instant> findLastTs(long metalId);

    long deleteBefore(Instant cutoff);
    int deleteByMetalAndRange(long metalId, Instant from, Instant to);

    // ---------- UPSERT ----------

    void upsertProto(long metalId, MetalRatesProto.SpotPrice price);
    void upsertProtoBatch(long metalId,List<MetalRatesProto.SpotPrice> prices);
    int deleteById(long id);

    // ---------- PROTO (NEW) ----------
    List<SpotTick5mEntity> findByMetalCodeProto(String metalCode, Instant from, Instant to);

    Page<SpotTick5mEntity> findIntradayProtoPage(
            long metalId, Instant from, Instant to, Pageable pageable
    );

}
