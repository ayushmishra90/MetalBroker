package com.hugoserve.metalbroker.service.impl;

import com.hugoserve.metalbroker.facade.DbMetalAdminFacade;
import com.hugoserve.metalbroker.proto.MetalRatesProto;
import com.hugoserve.metalbroker.service.DbMetalAdminService;
import com.hugoserve.metalbroker.utils.ProtoJson;
import com.hugoserve.metalbroker.utils.ProtoJsonParser;
import com.hugoserve.metalbroker.utils.constants.ErrorCodes;
import com.hugoserve.metalbroker.utils.exception.BusinessException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import static com.hugoserve.metalbroker.config.RedisCacheConfig.CACHE_DB_METALS;

@Service
public class DbMetalAdminServiceImpl implements DbMetalAdminService {

    private final DbMetalAdminFacade facade;

    public DbMetalAdminServiceImpl(DbMetalAdminFacade facade) {
        this.facade = facade;
    }

    // ================= CREATE =================
    @Override
    @Transactional
    @CacheEvict(cacheNames = CACHE_DB_METALS, allEntries = true)
    public String create(String json) {

        MetalRatesProto.MetalInfo req = parse(json);
        validateCreate(req);

        facade.create(
                req.getMetal().name(),
                req.getName(),
                req.getIsActive()
        );

        return adminOk("METAL_CREATED", "metal created");
    }

    // ================= UPDATE =================
    @Override
    @Transactional
    @CacheEvict(cacheNames = CACHE_DB_METALS, allEntries = true)
    public String update(String code, String json) {

        if (!StringUtils.hasText(code)) {
            throw new BusinessException(
                    ErrorCodes.INVALID_REQUEST,
                    "metal code required"
            );
        }

        MetalRatesProto.MetalInfo req = parse(json);

        String name = StringUtils.hasText(req.getName())
                ? req.getName()
                : null;

        Boolean active =
                json.contains("\"isActive\"") ||
                        json.contains("\"is_active\"")
                        ? req.getIsActive()
                        : null;

        facade.update(
                normalize(code),
                name,
                active
        );

        return adminOk("METAL_UPDATED", "metal updated");
    }

    // ================= DEACTIVATE =================
    @Override
    @Transactional
    @CacheEvict(cacheNames = CACHE_DB_METALS, allEntries = true)
    public String deactivate(String code) {

        if (!StringUtils.hasText(code)) {
            throw new BusinessException(
                    ErrorCodes.INVALID_REQUEST,
                    "metal code required"
            );
        }

        facade.deactivate(normalize(code));

        return adminOk(
                "METAL_DEACTIVATED",
                "metal deactivated"
        );
    }

    // ================= HELPERS =================
    private MetalRatesProto.MetalInfo parse(String json) {
        try {
            return ProtoJsonParser
                    .parse(json, MetalRatesProto.MetalInfo.newBuilder())
                    .build();
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCodes.ADMIN_INVALID_PAYLOAD,
                    "invalid proto json"
            );
        }
    }

    private void validateCreate(MetalRatesProto.MetalInfo req) {

        if (req.getMetal()
                == MetalRatesProto.Metal.METAL_UNSPECIFIED) {

            throw new BusinessException(
                    ErrorCodes.INVALID_REQUEST,
                    "metal must be specified"
            );
        }

        if (!StringUtils.hasText(req.getName())) {
            throw new BusinessException(
                    ErrorCodes.INVALID_REQUEST,
                    "metal name required"
            );
        }
    }

    private String normalize(String code) {
        return code.trim().toUpperCase();
    }

    private String adminOk(String code, String msg) {
        return ProtoJson.print(
                MetalRatesProto.AdminWriteResult
                        .newBuilder()
                        .setOk(true)
                        .setMessage(msg)
                        .build()
        );
    }
}
