package com.hugoserve.metalbroker.service;
import com.hugoserve.metalbroker.proto.MetalRatesProto;

public interface TradeService {

    MetalRatesProto.TradeResponse trade(MetalRatesProto.TradeRequest req);

    String tradeHistory();
}
