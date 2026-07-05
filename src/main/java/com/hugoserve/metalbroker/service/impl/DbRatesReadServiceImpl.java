package com.hugoserve.metalbroker.service.impl;

import static com.hugoserve.metalbroker.config.RedisCacheConfig.CACHE_DB_LATEST;

import com.hugoserve.metalbroker.facade.DbMetalAdminFacade;
import com.hugoserve.metalbroker.facade.DbRatesReadFacade;
import com.hugoserve.metalbroker.proto.MetalRatesProto;
import com.hugoserve.metalbroker.service.DbRatesReadService;
import com.hugoserve.metalbroker.utils.DbProtoMapper;
import com.hugoserve.metalbroker.utils.ProtoJson;
import com.hugoserve.metalbroker.utils.constants.ErrorCodes;
import com.hugoserve.metalbroker.utils.exception.BusinessException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;

@Service
public class DbRatesReadServiceImpl implements DbRatesReadService {

    private final DbRatesReadFacade facade;
    private final DbProtoMapper mapper;
    private final DbMetalAdminFacade metalFacade;

    public DbRatesReadServiceImpl(
            DbRatesReadFacade facade,
            DbProtoMapper mapper,
            DbMetalAdminFacade metalFacade
    ) {
        this.facade = facade;
        this.mapper = mapper;
        this.metalFacade = metalFacade;
    }

    // ---------------- LATEST ----------------
    @Override

    @Cacheable(cacheNames = CACHE_DB_LATEST, key = "#metal.trim().toUpperCase()")
    public String latest(String metal) {
        String code = validateMetal(metal);

        var entity = facade.latestProto(code)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCodes.NOT_METAL,
                                "metal not found"
                        ));

        return ProtoJson.print(
                mapper.latestSnapshotFromProto(entity)
        );
    }

    // ---------------- INTRADAY ----------------
    @Override
    public String intraday(String metal, Instant from, Instant to) {
        String code = validateMetal(metal);

        var b = MetalRatesProto.Tick5mList.newBuilder()
                .setMetal(mapper.toMetalEnum(code));

        facade.intradayProto(code, from, to)
                .forEach(e ->
                        b.addItems(mapper.tick5mFromProto(code, e))
                );

        return ProtoJson.print(b.build());
    }

    // ---------------- HISTORY ----------------
    @Override
    public String history(String metal, LocalDate from, LocalDate to) {
        String code = validateMetal(metal);

        long metalId = metalFacade.findIdByCode(code)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCodes.NOT_METAL,
                                "metal not found"
                        ));

        var b = MetalRatesProto.DailyHistoryList.newBuilder()
                .setMetal(mapper.toMetalEnum(code));

        facade.historyProto(metalId, from, to)
                .forEach(e ->
                        b.addItems(
                                mapper.dailyHistoryCandleFromProto(code, e)
                        )
                );

        return ProtoJson.print(b.build());
    }

    // ---------------- INTRADAY PAGE ----------------
    @Override
    public String intradayPage(
            String metal,
            Instant from,
            Instant to,
            int page,
            int size,
            String sort,
            String dir
    ) {
        String code = validateMetal(metal);

        long metalId = metalFacade.findIdByCode(code)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCodes.NOT_METAL,
                                "metal not found"
                        ));

        PageRequest pr = pageRequest(page, size, sort, dir);
        var p = facade.intradayProtoPage(metalId, from, to, pr);

        var b = MetalRatesProto.Tick5mPage.newBuilder()
                .setMetal(mapper.toMetalEnum(code))
                .setPage(pageInfo(p));

        p.forEach(e ->
                b.addItems(mapper.tick5mFromProto(code, e))
        );

        return ProtoJson.print(b.build());
    }

    // ---------------- HISTORY PAGE ----------------
    @Override
    public String historyPage(
            String metal,
            LocalDate from,
            LocalDate to,
            int page,
            int size,
            String sort,
            String dir
    ) {
        String code = validateMetal(metal);

        long metalId = metalFacade.findIdByCode(code)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCodes.NOT_METAL,
                                "metal not found"
                        ));

        PageRequest pr = pageRequest(page, size, sort, dir);
        var p = facade.historyProtoPage(metalId, from, to, pr);

        var b = MetalRatesProto.DailyHistoryPage.newBuilder()
                .setMetal(mapper.toMetalEnum(code))
                .setPage(pageInfo(p));

        p.forEach(e ->
                b.addItems(
                        mapper.dailyHistoryCandleFromProto(code, e)
                )
        );

        return ProtoJson.print(b.build());
    }

    // ---------------- HELPERS ----------------
    private String validateMetal(String metal) {
        if (!StringUtils.hasText(metal)) {
            throw new BusinessException(
                    ErrorCodes.BAD_REQUEST,
                    "metal required"
            );
        }
        return metal.trim().toUpperCase();
    }

    private PageRequest pageRequest(
            int page,
            int size,
            String sort,
            String dir
    ) {
        if (!StringUtils.hasText(sort)) {
            return PageRequest.of(page, size);
        }
        return PageRequest.of(
                page,
                size,
                "desc".equalsIgnoreCase(dir)
                        ? Sort.by(sort).descending()
                        : Sort.by(sort).ascending()
        );
    }

    private MetalRatesProto.PageInfo pageInfo(
            org.springframework.data.domain.Page<?> p
    ) {
        return MetalRatesProto.PageInfo.newBuilder()
                .setPage(p.getNumber())
                .setSize(p.getSize())
                .setTotalElements(p.getTotalElements())
                .setTotalPages(p.getTotalPages())
                .build();
    }
}
