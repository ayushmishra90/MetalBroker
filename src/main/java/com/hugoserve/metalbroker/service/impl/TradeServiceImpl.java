package com.hugoserve.metalbroker.service.impl;

import com.hugoserve.metalbroker.facade.TradeFacade;
import com.hugoserve.metalbroker.facade.WalletFacade;
import com.hugoserve.metalbroker.facade.WalletMetalFacade;
import com.hugoserve.metalbroker.proto.MetalRatesProto;
import com.hugoserve.metalbroker.security.SecurityUser;
import com.hugoserve.metalbroker.service.TradeService;
import com.hugoserve.metalbroker.service.WalletService;
import com.hugoserve.metalbroker.service.proto.auth.UserEntity;
import com.hugoserve.metalbroker.service.proto.trade.TradeEntity;
import com.hugoserve.metalbroker.service.proto.wallet.WalletEntity;
import com.hugoserve.metalbroker.utils.ProtoJson;
import com.hugoserve.metalbroker.utils.UserProtoMapper;
import com.hugoserve.metalbroker.utils.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
@Service
@RequiredArgsConstructor
@Transactional
public class TradeServiceImpl implements TradeService {

    private final TradeFacade facade;
    private final WalletFacade walletFacade;
    private final WalletService walletService;
    private final WalletMetalFacade walletMetalFacade;

    // ===================== TRADE =====================
    @Override
    public MetalRatesProto.TradeResponse trade(MetalRatesProto.TradeRequest req) {

        UserEntity user = currentUser();
        WalletEntity wallet = walletService.getOrCreateWallet();

        var latest = facade.latestPrice(req.getMetal().name())
                .orElseThrow(() ->
                        new BusinessException("TRADE_PRICE_UNAVAILABLE", "Metal price not available"));

        double price = latest.getMid();

        // -------- quantity calculation --------
        BigDecimal quantity;
        if (req.getQuantity() > 0) {
            quantity = BigDecimal.valueOf(req.getQuantity());
        } else if (req.getAmount() > 0) {
            quantity = BigDecimal.valueOf(req.getAmount())
                    .divide(BigDecimal.valueOf(price), 6, RoundingMode.DOWN);
        } else {
            throw new BusinessException("TRADE_INVALID", "Quantity or amount required");
        }

        BigDecimal total = quantity.multiply(BigDecimal.valueOf(price));
        double walletBalance = wallet.getInrBalance();

        // ===================== BUY =====================
        if (req.getType() == MetalRatesProto.TradeType.BUY) {

            if (walletBalance < total.doubleValue()) {
                throw new BusinessException(
                        "TRADE_INSUFFICIENT_BALANCE",
                        "Insufficient wallet balance"
                );
            }

            double updatedBalance = walletBalance - total.doubleValue();

            walletFacade.updateBalance(wallet.getWalletId(), updatedBalance);

            facade.saveTrade(
                    user.getId(),
                    "BUY",
                    req.getMetal().name(),
                    quantity.doubleValue(),
                    price,
                    total.doubleValue()
            );

            return MetalRatesProto.TradeResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Buy trade executed successfully")
                    .setExecutedPrice(price)
                    .setTotalCost(total.doubleValue())
                    .setWalletBalance(updatedBalance)
                    .build();
        }

        // ===================== SELL =====================
        if (req.getType() == MetalRatesProto.TradeType.SELL) {

            double ownedQty = walletMetalFacade.getQuantity(user.getId(), req.getMetal().name()).orElse(0.0);

            if (ownedQty <= 0) {
                throw new BusinessException(
                        "TRADE_NO_HOLDINGS",
                        "No holdings available to sell"
                );
            }

            if (quantity.doubleValue() > ownedQty) {
                throw new BusinessException(
                        "TRADE_INSUFFICIENT_QUANTITY",
                        "Sell quantity exceeds owned quantity"
                );
            }

            double updatedBalance = walletBalance + total.doubleValue();

            walletFacade.updateBalance(wallet.getWalletId(), updatedBalance);

            facade.saveTrade(
                    user.getId(),
                    "SELL",
                    req.getMetal().name(),
                    quantity.doubleValue(),
                    price,
                    total.doubleValue()
            );

            return MetalRatesProto.TradeResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Sell trade executed successfully")
                    .setExecutedPrice(price)
                    .setTotalCost(total.doubleValue())
                    .setWalletBalance(updatedBalance)
                    .build();
        }

        // ===================== FALLBACK =====================
        throw new BusinessException("TRADE_INVALID", "Unsupported trade type");
    }



    // ===================== HISTORY =====================
    @Override
    public String tradeHistory() {
        UserEntity user = currentUser();

        var trades = facade.tradeHistory(user.getId());

        var resp = MetalRatesProto.TradeHistoryResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Trade history fetched");

        for (TradeEntity t : trades) {
            resp.addTrades(
                    MetalRatesProto.TradeHistoryItem.newBuilder()
                            .setTradeId(String.valueOf(t.getId()))
                            .setType(MetalRatesProto.TradeType.valueOf(t.getType()))
                            .setMetal(MetalRatesProto.Metal.valueOf(t.getMetal()))
                            .setQuantity(t.getQuantity())
                            .setExecutedPrice(t.getExecutedPrice())
                            .setTotalAmount(t.getTotalAmount())
                            .setExecutedAtUtc(t.getExecutedAtUtc())
                            .build()
            );
        }

        return ProtoJson.print(resp.build());
    }

    // ===================== HELPERS =====================
    private UserEntity currentUser() {
        Object principal =
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getPrincipal();

        if (principal instanceof SecurityUser su) {
            return UserProtoMapper.toProto(su.getUser());
        }

        throw new BusinessException(
                "AUTH_REQUIRED",
                "User not authenticated"
        );
    }



    private MetalRatesProto.TradeResponse fail(String msg) {
        return MetalRatesProto.TradeResponse.newBuilder()
                .setSuccess(false)
                .setMessage(msg)
                .build();
    }
}
