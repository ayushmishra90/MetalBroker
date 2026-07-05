package com.hugoserve.metalbroker.facade;

import com.hugoserve.metalbroker.domain.dao.SpotTick5mDAO;
import com.hugoserve.metalbroker.proto.MetalRatesProto;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DbTicksAdminFacade {

    private final SpotTick5mDAO tickDao;

    public DbTicksAdminFacade( SpotTick5mDAO tickDao) {
        this.tickDao = tickDao;
    }

    // ---------- UPSERT ----------
    public void upsert(long metalId,MetalRatesProto.SpotPrice price) {
        tickDao.upsertProto(metalId,price);
    }

    // ---------- DELETE BY ID ----------
    public int deleteById(long id) {
        return tickDao.deleteById(id);
    }

    // ---------- DELETE RANGE ----------
    public int deleteRange(Long metalId, Instant from, Instant to) {
        return tickDao.deleteByMetalAndRange(metalId, from, to);
    }
}

