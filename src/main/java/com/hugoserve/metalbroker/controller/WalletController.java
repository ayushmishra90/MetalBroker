package com.hugoserve.metalbroker.controller;

import com.hugoserve.metalbroker.service.WalletMetalService;
import com.hugoserve.metalbroker.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final WalletMetalService walletMetalService;

    @GetMapping("/balance")
    public ResponseEntity<String> balance() {
        return ResponseEntity.ok(walletService.checkBalance());
    }

    @PostMapping("/deposit")
    public ResponseEntity<String> deposit(@RequestParam BigDecimal amount) {
        return ResponseEntity.ok(walletService.deposit(amount));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(@RequestParam BigDecimal amount) {
        return ResponseEntity.ok(walletService.withdraw(amount));
    }

    @GetMapping("/summary")
    public ResponseEntity<String> summary() {
        return ResponseEntity.ok(walletMetalService.getWalletSummary());
    }
}
