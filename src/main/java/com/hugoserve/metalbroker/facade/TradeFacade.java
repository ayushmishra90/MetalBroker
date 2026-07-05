package com.hugoserve.metalbroker.facade;

import com.hugoserve.metalbroker.domain.dao.TradeDAO;
import com.hugoserve.metalbroker.domain.dao.MetalLatestDAO;
import com.hugoserve.metalbroker.service.proto.metal.MetalLatestEntity;
import com.hugoserve.metalbroker.service.proto.trade.TradeEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
@Component
public class TradeFacade {

    private final TradeDAO tradeDao;
    private final MetalLatestDAO latestDao;

    public TradeFacade(TradeDAO tradeDao, MetalLatestDAO latestDao) {
        this.tradeDao = tradeDao;
        this.latestDao = latestDao;
    }

    // ---------- PRIMARY (PROTO) ----------
    public Optional<MetalLatestEntity> latestPrice(String metalCode) {
        return latestDao.findByMetalCodeProto(metalCode);
    }

    public TradeEntity saveTrade(
            long userId,
            String type,
            String metal,
            double quantity,
            double price,
            double total
    ) {
        return tradeDao.insertProto(userId, type, metal, quantity, price, total);
    }

    public List<TradeEntity> tradeHistory(long userId) {
        return tradeDao.findByUserProto(userId);
    }
}


