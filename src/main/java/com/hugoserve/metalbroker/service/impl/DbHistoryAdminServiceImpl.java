package com.hugoserve.metalbroker.service.impl;

import com.hugoserve.metalbroker.facade.DbHistoryAdminFacade;
import com.hugoserve.metalbroker.facade.DbMetalAdminFacade;
import com.hugoserve.metalbroker.proto.MetalRatesProto;
import com.hugoserve.metalbroker.service.DbHistoryAdminService;
import com.hugoserve.metalbroker.utils.ProtoJson;
import com.hugoserve.metalbroker.utils.ProtoJsonParser;
import com.hugoserve.metalbroker.utils.constants.ErrorCodes;
import com.hugoserve.metalbroker.utils.exception.BusinessException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

@Service
public class DbHistoryAdminServiceImpl implements DbHistoryAdminService {

    private final DbHistoryAdminFacade facade;
    private final DbMetalAdminFacade metalFacade;

    public DbHistoryAdminServiceImpl(DbHistoryAdminFacade facade, DbMetalAdminFacade metalFacade) {
        this.facade = facade;
        this.metalFacade = metalFacade;
    }

    // ------------------ UPSERT ONE ------------------
    @Override
    @Transactional
    public String upsertOne(String json) {
        MetalRatesProto.DailyHistoryCandle req;
        try {
            req = ProtoJsonParser.parse(
                    json,
                    MetalRatesProto.DailyHistoryCandle.newBuilder()
            ).build();
        } catch (RuntimeException e) {
            throw new BusinessException(
                    ErrorCodes.ADMIN_INVALID_PAYLOAD,
                    "Invalid proto json"
            );
        }

        validate(req);
        Long metalId = metalFacade.findIdByCode(req.getMetal().name()).orElse(null);;
        facade.upsert(metalId, req);
        return ProtoJson.print(req);
    }

    // ------------------ UPSERT BULK ------------------
    @Override
    @Transactional
    public String upsertBulk(String json) {
        MetalRatesProto.DailyHistoryList req;
        try {
            req = ProtoJsonParser.parse(
                    json,
                    MetalRatesProto.DailyHistoryList.newBuilder()
            ).build();
        } catch (RuntimeException e) {
            throw new BusinessException(
                    ErrorCodes.ADMIN_INVALID_PAYLOAD,
                    "Invalid proto json"
            );
        }
        int affected = 0;

        for (var item : req.getItemsList()) {
            if (!StringUtils.hasText(item.getDayUtc())) {
                continue;
            }
            Long metalId = metalFacade.findIdByCode(req.getMetal().name()).orElse(null);;
            facade.upsert(metalId, item);
            affected++;
        }

        return ProtoJson.print(
                MetalRatesProto.AdminWriteResult.newBuilder()
                        .setOk(true)
                        .setMessage("bulk upsert completed")
                        .setAffected(affected)
                        .build()
        );
    }

    // ------------------ DELETE ONE ------------------
    @Override
    @Transactional
    public String deleteOne(String metalCode, LocalDate dayUtc) {
        Long metalId = metalFacade.findIdByCode(metalCode).orElse(null);
        int deleted = facade.deleteOne(metalId, dayUtc);
        return ProtoJson.print(
                MetalRatesProto.AdminWriteResult.newBuilder()
                        .setOk(deleted > 0)
                        .setMessage("deleted day=" + dayUtc)
                        .setAffected(deleted)
                        .build()
        );
    }

    // ------------------ DELETE RANGE ------------------
    @Override
    @Transactional
    public String deleteRange(String metalCode, LocalDate fromDay, LocalDate toDay) {
        Long metalId = metalFacade.findIdByCode(metalCode).orElse(null);
        int deleted = facade.deleteRange(metalId, fromDay, toDay);
        return ProtoJson.print(
                MetalRatesProto.AdminWriteResult.newBuilder()
                        .setOk(true)
                        .setMessage("deleted")
                        .setAffected(deleted)
                        .build()
        );
    }

    // ------------------ VALIDATION ------------------
    private void validate(MetalRatesProto.DailyHistoryCandle req) {
        if (req.getMetal() == MetalRatesProto.Metal.METAL_UNSPECIFIED ||
                !StringUtils.hasText(req.getDayUtc())) {
            throw new BusinessException(
                    ErrorCodes.INVALID_REQUEST,
                    "metal and dayUtc required"
            );
        }
    }
}


