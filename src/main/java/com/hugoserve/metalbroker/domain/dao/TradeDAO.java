package com.hugoserve.metalbroker.domain.dao;

import com.hugoserve.metalbroker.service.proto.trade.TradeEntity;

import java.util.List;

public interface TradeDAO {

    TradeEntity insertProto(
            long userId,
            String type,
            String metal,
            double quantity,
            double executedPrice,
            double totalAmount
    );

    List<TradeEntity> findByUserProto(long userId);
}
