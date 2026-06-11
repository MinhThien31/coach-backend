package com.minhthien.web.coach.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhthien.web.coach.dto.response.WalletHistoryItemResponse;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.entity.Wallet;
import com.minhthien.web.coach.entity.WalletTopUpOrder;
import com.minhthien.web.coach.entity.WalletTransaction;
import com.minhthien.web.coach.enums.UserRole;
import com.minhthien.web.coach.enums.WalletTopUpOrderStatus;
import com.minhthien.web.coach.enums.WalletTransactionType;
import com.minhthien.web.coach.repository.*;
import com.minhthien.web.coach.service.impl.WalletServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletHistoryServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private WalletRepository walletRepository;
    @Mock private UserBankAccountRepository userBankAccountRepository;
    @Mock private WalletTransactionRepository walletTransactionRepository;
    @Mock private WalletTopUpOrderRepository walletTopUpOrderRepository;
    @Mock private PlatformSettingsRepository platformSettingsRepository;
    @Mock private UserSubscriptionRepository userSubscriptionRepository;
    @Mock private PayOSGatewayService payOSGatewayService;

    @Test
    void historyKeepsPendingTopUpAndSkipsPaidTopUpOrder() {
        User user = User.builder()
                .id(3L)
                .username("wallet-user")
                .email("wallet@example.com")
                .role(UserRole.TRAINEES)
                .build();
        Wallet wallet = Wallet.builder().id(4L).user(user).balance(200_000L).currency("VND").build();
        WalletTransaction paidTopUpTransaction = WalletTransaction.builder()
                .id(21L)
                .wallet(wallet)
                .type(WalletTransactionType.TOP_UP)
                .amount(200_000L)
                .balanceBefore(0L)
                .balanceAfter(200_000L)
                .createdAt(LocalDateTime.of(2026, 6, 10, 10, 0))
                .build();
        WalletTopUpOrder paidOrder = WalletTopUpOrder.builder()
                .id(31L)
                .user(user)
                .wallet(wallet)
                .orderCode(1001L)
                .amount(200_000L)
                .description("Paid")
                .status(WalletTopUpOrderStatus.PAID)
                .createdAt(LocalDateTime.of(2026, 6, 10, 9, 59))
                .build();
        WalletTopUpOrder pendingOrder = WalletTopUpOrder.builder()
                .id(32L)
                .user(user)
                .wallet(wallet)
                .orderCode(1002L)
                .amount(500_000L)
                .description("Pending")
                .status(WalletTopUpOrderStatus.PENDING)
                .createdAt(LocalDateTime.of(2026, 6, 11, 10, 0))
                .build();

        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(walletRepository.findByUserId(3L)).thenReturn(Optional.of(wallet));
        when(walletTransactionRepository.findByWalletIdOrderByCreatedAtDesc(4L))
                .thenReturn(List.of(paidTopUpTransaction));
        when(walletTopUpOrderRepository.findByUserIdOrderByCreatedAtDesc(3L))
                .thenReturn(List.of(pendingOrder, paidOrder));

        WalletServiceImpl service = new WalletServiceImpl(
                userRepository,
                walletRepository,
                userBankAccountRepository,
                walletTransactionRepository,
                walletTopUpOrderRepository,
                platformSettingsRepository,
                userSubscriptionRepository,
                payOSGatewayService,
                new ObjectMapper()
        );

        Page<WalletHistoryItemResponse> result =
                service.getMyTransactions(3L, null, null, null, null, 0, 10);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(WalletHistoryItemResponse::getId)
                .containsExactly("TOP_UP_ORDER-32", "TRANSACTION-21");
        assertThat(result.getContent())
                .noneMatch(item -> item.getId().equals("TOP_UP_ORDER-31"));
    }
}
