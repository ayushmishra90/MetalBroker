package com.hugoserve.metalbroker.facade;

import java.util.Optional;

import com.hugoserve.metalbroker.domain.dao.MetalDAO;
import org.springframework.stereotype.Component;

@Component
public class DbMetalAdminFacade {

    private final MetalDAO metalDao;

    public DbMetalAdminFacade(MetalDAO metalDao) {
        this.metalDao = metalDao;
    }

    // ---------- CREATE ----------
    public void create(String code, String name, boolean active) {
        metalDao.upsert(code, name, active);
    }

    // ---------- UPDATE ----------
    public void update(String code, String name, Boolean active) {
        metalDao.update(code, name, active);
    }

    // ---------- DEACTIVATE ----------
    public void deactivate(String code) {
        metalDao.update(code, null, false);
    }

    // ---------- READ HELP ----------
    public Optional<Long> findIdByCode(String code) {
        return metalDao.findIdByCode(code);
    }
}
