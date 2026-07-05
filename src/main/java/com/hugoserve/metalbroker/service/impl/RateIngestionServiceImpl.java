package com.hugoserve.metalbroker.service.impl;

import com.hugoserve.metalbroker.facade.DbMetalAdminFacade;
import com.hugoserve.metalbroker.facade.RateIngestionFacade;
import com.hugoserve.metalbroker.proto.MetalRatesProto;
import com.hugoserve.metalbroker.service.RateIngestionService;
import com.hugoserve.metalbroker.utils.TimeUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class RateIngestionServiceImpl implements RateIngestionService {

    private final RateIngestionFacade facade;
    private final DbMetalAdminFacade metalFacade;

    public RateIngestionServiceImpl(RateIngestionFacade facade, DbMetalAdminFacade metalFacade) {

        this.facade = facade;
        this.metalFacade = metalFacade;
    }

    private long metalId(String code) {
        return metalFacade.findIdByCode(code).orElse(null);
    }

    private Instant toInstant(MetalRatesProto.SpotPrice p) {
        return Instant.ofEpochSecond(
                p.getDate().getSeconds(),
                p.getDate().getNanos()
        );
    }

    @Override
    @Transactional
    public void ingestLatestAndTick(String metalCode, MetalRatesProto.SpotPrice dto) {
        if (dto == null) return;

        long metalId = metalId(metalCode);

        facade.upsertLatest(metalId, dto);
        facade.upsertTick(metalId, dto);

    }

    @Override
//    @Transactional
    public void ingestIntraday(String metalCode, List<MetalRatesProto.SpotPrice> items) {
        if (items == null || items.isEmpty()) return;

        long metalId = metalId(metalCode);


        facade.upsertTicksBatch(metalId, items);
    }


    @Override
    @Transactional
    public void ingestDailyHistory(
            String metalCode,
            List<MetalRatesProto.SpotPriceItem> items,
            int lastNDaysOnly
    ) {
        if (items == null || items.isEmpty()) return;

        long metalId = metalId(metalCode);
        LocalDate maxDay = facade.findMaxCandleDay(metalId);

        List<MetalRatesProto.SpotPriceItem> list =
                (lastNDaysOnly > 0 && items.size() > lastNDaysOnly)
                        ? items.subList(items.size() - lastNDaysOnly, items.size())
                        : items;

        for (var i : list) {
            LocalDate day = LocalDate.parse(i.getDate());
            if (maxDay != null && day.isBefore(maxDay)) continue;

            facade.upsertDailyHistory(
                    metalId,
                    MetalRatesProto.DailyHistoryCandle.newBuilder()
                            .setDayUtc(i.getDate())
                            .setOpen(i.getOpen())
                            .setHigh(i.getHigh())
                            .setLow(i.getLow())
                            .setClose(i.getClose())
                            .setMa50(i.getMa50())
                            .setMa200(i.getMa200())
                            .build()
            );
        }
    }

    @Override
    public Optional<Instant> getFeedTsUtc(String metalCode) {
        long metalId = metalId(metalCode);
        return facade.findLatestFeedTs(metalId);
    }

    @Override
    public Optional<Instant> getLatestFeedTsUtcUncached(String metalCode) {
        long metalId = metalId(metalCode);
        return facade.findLatestFeedTs(metalId);
    }

    @Override
    @Transactional
    public long cleanupOldIntraday(int retentionDays) {
        Instant cutoff = Instant.now().minus(Duration.ofDays(retentionDays));
        return facade.deleteTicksBefore(cutoff);
    }
}
