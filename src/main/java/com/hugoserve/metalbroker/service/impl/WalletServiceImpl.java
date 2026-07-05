package com.hugoserve.metalbroker.service.impl;

import com.hugoserve.metalbroker.facade.WalletFacade;
import com.hugoserve.metalbroker.proto.MetalRatesProto;
import com.hugoserve.metalbroker.security.SecurityUser;
import com.hugoserve.metalbroker.service.WalletService;
import com.hugoserve.metalbroker.service.proto.auth.UserEntity;
import com.hugoserve.metalbroker.service.proto.wallet.WalletEntity;
import com.hugoserve.metalbroker.utils.ProtoJson;
import com.hugoserve.metalbroker.utils.UserProtoMapper;
import com.hugoserve.metalbroker.utils.constants.ErrorCodes;
import com.hugoserve.metalbroker.utils.exception.BusinessException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class WalletServiceImpl implements WalletService {

    private final WalletFacade walletFacade;

    @Override
    public WalletEntity getOrCreateWallet() {
        UserEntity user = currentUser();

        return walletFacade.findByUser(user.getId())
                .orElseGet(() -> walletFacade.create(user.getId()));
    }

    @Override
    public String checkBalance() {
        WalletEntity wallet = getOrCreateWallet();
        return ok(true, "Balance fetched", wallet.getInrBalance());
    }

    @Override
    public String deposit(BigDecimal amount) {
        WalletEntity wallet = getOrCreateWallet();

        if (amount == null || amount.signum() <= 0) {
            throw new BusinessException(
                    ErrorCodes.INVALID_REQUEST,
                    "Invalid deposit amount"
            );
        }
        double updated = wallet.getInrBalance() + amount.doubleValue();
        walletFacade.updateBalance(wallet.getWalletId(), updated);

        return ok(true, "Deposit successful", updated);
    }

    @Override
    public String withdraw(BigDecimal amount) {
        WalletEntity wallet = getOrCreateWallet();

        if (amount == null || amount.signum() <= 0) {
            throw new BusinessException(
                    ErrorCodes.INVALID_REQUEST,
                    "Invalid withdrawal amount"
            );
        }

        if (wallet.getInrBalance() < amount.doubleValue()) {
            throw new BusinessException(
                    ErrorCodes.WALLET_NO_FUNDS,
                    "Insufficient INR balance"
            );
        }

        double updated = wallet.getInrBalance() - amount.doubleValue();
        walletFacade.updateBalance(wallet.getWalletId(), updated);

        return ok(true, "Withdrawal successful", updated);
    }

    // ---------- helpers ----------
    private UserEntity currentUser() {
        Object principal =
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof SecurityUser su) {
            return UserProtoMapper.toProto(su.getUser());
        }

        throw new BusinessException(
                ErrorCodes.AUTH_FAIL,
                "User not authenticated"
        );
    }

    private String ok(boolean success, String msg, double balance) {
        return ProtoJson.print(
                MetalRatesProto.WalletResponse.newBuilder()
                        .setSuccess(success)
                        .setMessage(msg)
                        .setCashBalance(balance)
                        .build()
        );
    }
}
