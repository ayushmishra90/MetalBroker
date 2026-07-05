package com.hugoserve.metalbroker.service;

import com.hugoserve.metalbroker.service.proto.wallet.WalletEntity;

import java.math.BigDecimal;

public interface WalletService {

    WalletEntity getOrCreateWallet();

    String checkBalance();

    String deposit( BigDecimal amount);

    String withdraw( BigDecimal amount);
}
