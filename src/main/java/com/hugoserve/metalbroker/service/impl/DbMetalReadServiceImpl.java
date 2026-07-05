package com.hugoserve.metalbroker.service.impl;

import static com.hugoserve.metalbroker.config.RedisCacheConfig.CACHE_DB_METALS;

import com.hugoserve.metalbroker.facade.DbMetalReadFacade;
import com.hugoserve.metalbroker.proto.MetalRatesProto;
import com.hugoserve.metalbroker.service.DbMetalReadService;
import com.hugoserve.metalbroker.utils.DbProtoMapper;
import com.hugoserve.metalbroker.utils.ProtoJson;
import com.hugoserve.metalbroker.utils.constants.ErrorCodes;
import com.hugoserve.metalbroker.utils.exception.BusinessException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DbMetalReadServiceImpl implements DbMetalReadService {

    private final DbMetalReadFacade facade;
    private final DbProtoMapper mapper;

    public DbMetalReadServiceImpl(DbMetalReadFacade facade, DbProtoMapper mapper) {
        this.facade = facade;
        this.mapper = mapper;
    }

    // ---------------- LIST ----------------
    @Override
    @Cacheable(cacheNames = CACHE_DB_METALS, key = "'all'")
    public String list() {
        var builder = MetalRatesProto.MetalList.newBuilder();
        facade.listAll().forEach(m -> builder.addItems(mapper.metalInfo(m)));
        return ProtoJson.print(builder.build());
    }

    // ---------------- GET BY CODE ----------------
    @Override
    @Cacheable(cacheNames = CACHE_DB_METALS, key = "#code")
    public String getByCode(String code) {

        if (!StringUtils.hasText(code)) {
            throw new BusinessException(
                    ErrorCodes.BAD_REQUEST,
                    "metal code required"
            );
        }
        String normalized = code.trim().toUpperCase();
        var metal = facade.getByCode(normalized)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCodes.NOT_METAL,
                                "metal not found"
                        )
                );

        return ProtoJson.print(mapper.metalInfo(metal));
    }
}
