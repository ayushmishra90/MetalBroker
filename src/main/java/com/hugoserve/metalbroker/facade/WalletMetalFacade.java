package com.hugoserve.metalbroker.facade;

import com.hugoserve.metalbroker.domain.dao.MetalLatestDAO;
import com.hugoserve.metalbroker.domain.dao.WalletMetalDAO;
import com.hugoserve.metalbroker.service.proto.metal.MetalLatestEntity;
import com.hugoserve.metalbroker.service.proto.wallet.WalletMetalEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class WalletMetalFacade {

    private final WalletMetalDAO walletMetalDao;
    private final MetalLatestDAO metalLatestDao;

    public WalletMetalFacade(
            WalletMetalDAO walletMetalDao,
            MetalLatestDAO metalLatestDao
    ) {
        this.walletMetalDao = walletMetalDao;
        this.metalLatestDao = metalLatestDao;
    }

    // ---------- READ ----------
    public List<WalletMetalEntity> balances(long walletId) {
        return walletMetalDao.findAllByWalletProto(walletId);
    }

    public Optional<WalletMetalEntity> walletMetal(
            long walletId,
            String metal
    ) {
        return walletMetalDao.findProto(walletId, metal);
    }

    public Optional<MetalLatestEntity> latestPrice(String metal) {
        return metalLatestDao.findByMetalCodeProto(metal);
    }

    public Optional<Double> getQuantity(long walletId, String metal) {
        return walletMetalDao.findQuantity(walletId, metal);
    }
}

