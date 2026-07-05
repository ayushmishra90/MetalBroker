package com.hugoserve.metalbroker.facade;

import com.hugoserve.metalbroker.domain.dao.MetalDAO;
import com.hugoserve.metalbroker.service.proto.metal.MetalEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.hugoserve.metalbroker.config.RedisCacheConfig.CACHE_DB_METALS;

@Component
public class DbMetalReadFacade {

    private final MetalDAO metalDao;

    public DbMetalReadFacade(MetalDAO metalDao) {
        this.metalDao = metalDao;
    }

//    @Cacheable(cacheNames = CACHE_DB_METALS, key = "#code")
    public Optional<MetalEntity> getByCode(String code) {
        return metalDao.findByCodeProto(code);
    }
//    @Cacheable(cacheNames = CACHE_DB_METALS, key = "'all'")
    public List<MetalEntity> listAll() {
        return metalDao.findAllProto();
    }
}
