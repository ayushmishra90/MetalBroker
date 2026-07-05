package com.hugoserve.metalbroker.domain.dao;

import java.time.LocalDate;
import java.util.List;

import com.hugoserve.metalbroker.proto.MetalRatesProto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.hugoserve.metalbroker.service.proto.metal.MetalDailyHistoryProto.MetalDailyHistoryEntity;

public interface MetalDailyHistoryDAO {

        void upsertProto(long metalId, MetalRatesProto.DailyHistoryCandle candle);
        int deleteByMetalAndDay(long metalId, LocalDate dayUtc);

        int deleteByMetalAndRange(long metalId, LocalDate from, LocalDate to);

        LocalDate findMaxDayUtc(long metalId);


        List<MetalDailyHistoryEntity> findBetweenProto(
                long metalId,
                LocalDate from,
                LocalDate to
        );

        Page<MetalDailyHistoryEntity> findBetweenProtoPage(
                long metalId,
                LocalDate from,
                LocalDate to,
                Pageable pageable
        );

}

