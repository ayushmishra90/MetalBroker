package com.hugoserve.metalbroker.domain.dao;

import com.hugoserve.metalbroker.service.proto.wallet.WalletEntity;

import java.math.BigDecimal;
import java.util.Optional;

public interface WalletDAO {


    WalletEntity createProto(long userId);


    void updateBalance(long walletId, BigDecimal balance);


    // ---------- PROTO ----------
    Optional<WalletEntity> findByUserIdProto(long userId);

}
