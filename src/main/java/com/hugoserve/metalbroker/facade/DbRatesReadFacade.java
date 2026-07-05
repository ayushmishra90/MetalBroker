package com.hugoserve.metalbroker.facade;

import com.hugoserve.metalbroker.domain.dao.MetalLatestDAO;
import com.hugoserve.metalbroker.domain.dao.SpotTick5mDAO;
import com.hugoserve.metalbroker.domain.dao.MetalDailyHistoryDAO;
import com.hugoserve.metalbroker.service.proto.metal.MetalLatestEntity;
import com.hugoserve.metalbroker.service.proto.metal.SpotTick5mEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import com.hugoserve.metalbroker.service.proto.metal.MetalDailyHistoryProto.MetalDailyHistoryEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.hugoserve.metalbroker.config.RedisCacheConfig.CACHE_DB_LATEST;

@Component
public class DbRatesReadFacade {

    private final MetalLatestDAO latestDao;
    private final SpotTick5mDAO tickDao;
    private final MetalDailyHistoryDAO historyDao;

    public DbRatesReadFacade(
            MetalLatestDAO latestDao,
            SpotTick5mDAO tickDao,
            MetalDailyHistoryDAO historyDao
    ) {
        this.latestDao = latestDao;
        this.tickDao = tickDao;
        this.historyDao = historyDao;
    }

    // ---------- LATEST ----------
//    @Cacheable(cacheNames = CACHE_DB_LATEST, key = "#metalCode.trim().toUpperCase()")
    public Optional<MetalLatestEntity> latestProto(String metalCode) {
        return latestDao.findByMetalCodeProto(metalCode);
    }

    // ---------- INTRADAY ----------
    public List<SpotTick5mEntity> intradayProto(
            String metalCode, Instant from, Instant to
    ) {
        return tickDao.findByMetalCodeProto(metalCode, from, to);
    }

    public Page<SpotTick5mEntity> intradayProtoPage(
            long metalId, Instant from, Instant to, Pageable pageable
    ) {
        return tickDao.findIntradayProtoPage(metalId, from, to, pageable);
    }

    // ---------- HISTORY ----------
    public List<MetalDailyHistoryEntity> historyProto(
            long metalId,
            LocalDate from,
            LocalDate to
    ) {
        return historyDao.findBetweenProto(metalId, from, to);
    }

    public Page<MetalDailyHistoryEntity> historyProtoPage(
            long metalId,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    ) {
        return historyDao.findBetweenProtoPage(metalId, from, to, pageable);
    }
}
