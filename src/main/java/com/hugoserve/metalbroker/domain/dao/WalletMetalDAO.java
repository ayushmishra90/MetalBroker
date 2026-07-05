package com.hugoserve.metalbroker.domain.dao;

import com.hugoserve.metalbroker.service.proto.wallet.WalletMetalEntity;

import java.util.List;
import java.util.Optional;

public interface WalletMetalDAO {

    // ---------- PROTO ----------
    Optional<WalletMetalEntity> findProto(long walletId, String metal);
    List<WalletMetalEntity> findAllByWalletProto(long walletId);


    Optional<Double> findQuantity(long walletId, String metal);
}
