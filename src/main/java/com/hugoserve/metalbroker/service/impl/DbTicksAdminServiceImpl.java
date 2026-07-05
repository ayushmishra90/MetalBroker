package com.hugoserve.metalbroker.service.impl;

import static com.hugoserve.metalbroker.utils.DbProtoMapper.toSpotPrice;

import com.hugoserve.metalbroker.domain.dao.MetalDAO;
import com.hugoserve.metalbroker.facade.DbTicksAdminFacade;
import com.hugoserve.metalbroker.proto.MetalRatesProto;
import com.hugoserve.metalbroker.service.DbTicksAdminService;
import com.hugoserve.metalbroker.utils.ProtoJson;
import com.hugoserve.metalbroker.utils.ProtoJsonParser;
import com.hugoserve.metalbroker.utils.constants.ErrorCodes;
import com.hugoserve.metalbroker.utils.exception.BusinessException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
public class DbTicksAdminServiceImpl implements DbTicksAdminService {

    private final DbTicksAdminFacade facade;
    private final MetalDAO metalDao;

    public DbTicksAdminServiceImpl(DbTicksAdminFacade facade,MetalDAO metalDao) {
        this.facade = facade;
        this.metalDao = metalDao;
    }

    // ---------------- UPSERT ONE ----------------

    @Override
    @Transactional
    public String upsertOne(String json) {
        MetalRatesProto.Tick5m req = parseTick(json);
        validateSingle(req);
        MetalRatesProto.SpotPrice price = toSpotPrice(req);
        metalDao.findIdByCode(req.getMetal().name())
                .ifPresent(id -> facade.upsert(id, price));

        return ProtoJson.print(req);
    }

    // ---------------- UPSERT BULK ----------------
    @Override
    @Transactional
    public String upsertBulk(String json) {
        MetalRatesProto.Tick5mList req = parseTickList(json);

        if (req.getMetal() == MetalRatesProto.Metal.METAL_UNSPECIFIED) {
            throw new BusinessException(
                    ErrorCodes.INVALID_REQUEST,
                    "metal must be specified"
            );
        }

        var metalIdOpt = metalDao.findIdByCode(req.getMetal().name());
        if (metalIdOpt.isEmpty()) {
            return adminResult(true, "bulk upsert completed (metal ignored)", 0);
        }

        long metalId = metalIdOpt.get();
        List<MetalRatesProto.Tick5m> ticks = req.getItemsList().stream()
                .filter(MetalRatesProto.Tick5m::hasTsUtc)
                .sorted(Comparator.comparing(this::toInstant))
                .toList();
        ticks.forEach(t -> facade.upsert(metalId, toSpotPrice(t)));

        return adminResult(true, "bulk upsert completed (duplicates ignored)", ticks.size());
    }


    // ---------------- DELETE BY ID ----------------
    @Override
    @Transactional
    public String deleteById(long id) {
        int deleted = facade.deleteById(id);
        return adminResult(deleted > 0, "deleted id=" + id, deleted);
    }

    // ---------------- DELETE RANGE ----------------
    @Override
    @Transactional
    public String deleteRange(String metalCode, Instant from, Instant to) {
        int deleted = metalDao.findIdByCode(metalCode)
                .map(id -> facade.deleteRange(id, from, to))
                .orElse(0);

        return adminResult(true, "deleted=" + deleted, deleted);
    }


    // ---------------- HELPERS ----------------

    private MetalRatesProto.Tick5m parseTick(String json) {
        try {
            return ProtoJsonParser
                    .parse(json, MetalRatesProto.Tick5m.newBuilder())
                    .build();
        } catch (RuntimeException e) {
            throw new BusinessException(
                    ErrorCodes.INVALID_REQUEST,
                    "Invalid Proto Json"
            );
        }
    }

    private MetalRatesProto.Tick5mList parseTickList(String json) {
        try {
            return ProtoJsonParser
                    .parse(json, MetalRatesProto.Tick5mList.newBuilder())
                    .build();
        } catch (RuntimeException e) {
            throw new BusinessException(
                    ErrorCodes.INVALID_REQUEST,
                    "Invalid Proto Json"
            );
        }
    }

    private void validateSingle(MetalRatesProto.Tick5m req) {
        if (req.getMetal() == MetalRatesProto.Metal.METAL_UNSPECIFIED) {
            throw new BusinessException(
                    ErrorCodes.METAL_NOT_FOUND,
                    "metal must be XAU/XAG/XPD/XPT"
            );
        }
        if (!req.hasTsUtc()) {
            throw new BusinessException(
                    ErrorCodes.INVALID_REQUEST,
                    "tsUtc is required"
            );
        }
    }

    private Instant toInstant(MetalRatesProto.Tick5m p) {
        return Instant.ofEpochSecond(
                p.getTsUtc().getSeconds(),
                p.getTsUtc().getNanos()
        );
    }

    private String adminResult(boolean ok, String message, int affected) {
        return ProtoJson.print(
                MetalRatesProto.AdminWriteResult.newBuilder()
                        .setOk(ok)
                        .setMessage(message)
                        .setAffected(affected)
                        .build()
        );
    }
}
