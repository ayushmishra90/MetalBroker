package com.hugoserve.metalbroker.facade;

import com.hugoserve.metalbroker.domain.dao.SpotTick5mDAO;
import com.hugoserve.metalbroker.service.proto.metal.SpotTick5mEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class DbRateAnalysisFacade {

    private final SpotTick5mDAO tickDao;

    public DbRateAnalysisFacade(SpotTick5mDAO tickDao) {
        this.tickDao = tickDao;
    }

    // PROTO ONLY
    public List<SpotTick5mEntity> findIntraday(String metalCode, Instant from, Instant to
    ) {
        return tickDao.findByMetalCodeProto(metalCode, from, to);
    }
}
