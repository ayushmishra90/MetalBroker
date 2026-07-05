package com.hugoserve.metalbroker.service.impl;

import com.hugoserve.metalbroker.facade.WalletMetalFacade;
import com.hugoserve.metalbroker.proto.MetalRatesProto;
import com.hugoserve.metalbroker.service.WalletMetalService;
import com.hugoserve.metalbroker.service.WalletService;
import com.hugoserve.metalbroker.service.proto.wallet.WalletEntity;
import com.hugoserve.metalbroker.service.proto.wallet.WalletMetalEntity;
import com.hugoserve.metalbroker.utils.ProtoJson;
import com.hugoserve.metalbroker.utils.exception.BusinessException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class WalletMetalServiceImpl implements WalletMetalService {

    private final WalletService walletService;
    private final WalletMetalFacade facade;

    @Override
    public String getWalletSummary() {

        WalletEntity wallet = walletService.getOrCreateWallet();
        var balances = facade.balances(wallet.getWalletId());

        var summary =
                MetalRatesProto.WalletSummary.newBuilder()
                        .setWalletId(String.valueOf(wallet.getWalletId()))
                        .setCurrency(MetalRatesProto.Currency.INR)
                        .setCashBalance(wallet.getInrBalance());

        double totalEquity = wallet.getInrBalance();

        for (WalletMetalEntity bal : balances) {

            var latest = facade.latestPrice(bal.getMetal())
                    .orElseThrow(() -> new BusinessException(
                            "WALLET_PRICE_UNAVAILABLE",
                            "Latest price not available for metal: " + bal.getMetal()
                    ));

            double valuation = latest.getMid() * bal.getQuantity();
            totalEquity += valuation;

            summary.addBalance(
                    MetalRatesProto.WalletBalance.newBuilder()
                            .setMetal(MetalRatesProto.Metal.valueOf(bal.getMetal()))
                            .setBalance(bal.getQuantity())
                            .setValuation(valuation)
                            .setUnit(MetalRatesProto.WeightUnit.g)
                            .setCurrency(MetalRatesProto.Currency.INR)
                            .build()
            );
        }


        summary.setTotalEquity(totalEquity);
        return ProtoJson.print(summary.build());
    }
}

