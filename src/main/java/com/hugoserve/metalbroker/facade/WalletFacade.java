package com.hugoserve.metalbroker.facade;

import com.hugoserve.metalbroker.domain.dao.WalletDAO;
import com.hugoserve.metalbroker.service.proto.wallet.WalletEntity;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class WalletFacade {

    private final WalletDAO walletDao;

    public WalletFacade(WalletDAO walletDao) {
        this.walletDao = walletDao;
    }

    // ---------- READ ----------
    public Optional<WalletEntity> findByUser(long userId) {
        return walletDao.findByUserIdProto(userId);
    }

    // ---------- CREATE ----------
    public WalletEntity create(long userId) {
        return walletDao.createProto(userId);
    }

    // ---------- UPDATE ----------
    public void updateBalance(long walletId, double newBalance) {
        walletDao.updateBalance(walletId, BigDecimal.valueOf(newBalance));
    }
}

