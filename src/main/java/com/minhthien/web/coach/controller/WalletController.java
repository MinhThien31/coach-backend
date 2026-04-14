package com.minhthien.web.coach.controller;

import com.minhthien.web.coach.dto.request.WalletTopUpRequest;
import com.minhthien.web.coach.dto.response.*;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.exception.BadRequestException;
import com.minhthien.web.coach.service.WalletService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/api/v1/wallets/me")
    public ResponseEntity<ApiResponse<WalletResponse>> getMyWallet(
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Wallet loaded successfully",
                        walletService.getMyWallet(currentUser.getId())
                )
        );
    }

    @GetMapping("/api/v1/wallets/me/transactions")
    public ResponseEntity<ApiResponse<List<WalletTransactionResponse>>> getMyTransactions(
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Wallet transactions loaded successfully",
                        walletService.getMyTransactions(currentUser.getId())
                )
        );
    }

    @PostMapping("/api/v1/wallets/top-up")
    public ResponseEntity<ApiResponse<WalletTopUpResponse>> createTopUp(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody WalletTopUpRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "PayOS payment link created successfully",
                        walletService.createTopUpPayment(currentUser.getId(), request)
                )
        );
    }

    @GetMapping("/api/v1/wallets/top-up/{orderCode}")
    public ResponseEntity<ApiResponse<WalletTopUpResponse>> getTopUpStatus(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long orderCode
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Wallet top-up status loaded successfully",
                        walletService.getTopUpStatus(currentUser.getId(), orderCode)
                )
        );
    }

    @PostMapping("/api/v1/wallets/top-up/payos/webhook")
    @SecurityRequirement(name = "")
    public ResponseEntity<String> handlePayOSWebhook(@RequestBody Map<String, Object> payload) {
        try {
            walletService.handlePayOSWebhook(payload);
            return ResponseEntity.ok("OK");
        } catch (BadRequestException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.ok("IGNORED");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/v1/admin/wallets/overview")
    public ResponseEntity<ApiResponse<AdminWalletOverviewResponse>> getAdminWalletOverview() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Admin wallet overview loaded successfully",
                        walletService.getAdminWalletOverview()
                )
        );
    }
}
