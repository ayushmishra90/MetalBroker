package com.hugoserve.metalbroker.facade;

import com.hugoserve.metalbroker.domain.dao.MetalDailyHistoryDAO;
import com.hugoserve.metalbroker.proto.MetalRatesProto;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
@Component
public class DbHistoryAdminFacade {

    private final MetalDailyHistoryDAO historyDao;

    public DbHistoryAdminFacade(MetalDailyHistoryDAO historyDao) {
        this.historyDao = historyDao;
    }

    public void upsert(Long metalId, MetalRatesProto.DailyHistoryCandle candle) {
        historyDao.upsertProto(metalId, candle);
    }
    public int deleteOne(Long metalId, LocalDate day) {
        return historyDao.deleteByMetalAndDay(metalId, day);
    }

    public int deleteRange(Long metalId,LocalDate from, LocalDate to) {
        return historyDao.deleteByMetalAndRange(metalId, from, to);
    }
}