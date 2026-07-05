package com.hugoserve.metalbroker.facade;

import com.hugoserve.metalbroker.domain.dao.*;
import com.hugoserve.metalbroker.proto.MetalRatesProto;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class RateIngestionFacade {
    private final MetalLatestDAO latestDao;
    private final SpotTick5mDAO tickDao;
    private final MetalDailyHistoryDAO historyDao;

    public RateIngestionFacade(
            MetalLatestDAO latestDao,
            SpotTick5mDAO tickDao,
            MetalDailyHistoryDAO historyDao
    ) {
        this.latestDao = latestDao;
        this.tickDao = tickDao;
        this.historyDao = historyDao;
    }

    // ---------- LATEST ----------
    public void upsertLatest(long metalId, MetalRatesProto.SpotPrice price) {
        latestDao.upsertProto(metalId, price);
    }

    public Optional<Instant> findLatestFeedTs(Long metalId) {
        return tickDao.findLastTs(metalId);
    }

    // ---------- TICKS ----------
    public Optional<Instant> findLastTickTs(long metalId) {
        return tickDao.findLastTs(metalId);
    }

    public void upsertTick(long metalId, MetalRatesProto.SpotPrice price) {
        tickDao.upsertProto(metalId, price);
    }
    public void upsertTicksBatch(long metalId, List<MetalRatesProto.SpotPrice> prices) {
        tickDao.upsertProtoBatch(metalId, prices);
    }

    public long deleteTicksBefore(Instant cutoff) {
        return tickDao.deleteBefore(cutoff);
    }

    // ---------- DAILY ----------
    public void upsertDailyHistory(long metalId, MetalRatesProto.DailyHistoryCandle candle) {
        historyDao.upsertProto(metalId, candle);
    }

    public LocalDate findMaxCandleDay(long metalId) {
        return historyDao.findMaxDayUtc(metalId);
    }
}