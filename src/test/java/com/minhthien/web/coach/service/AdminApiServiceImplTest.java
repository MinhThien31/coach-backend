package com.minhthien.web.coach.service;

import com.minhthien.web.coach.dto.response.AdminApiResponses;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.entity.Wallet;
import com.minhthien.web.coach.entity.WalletTransaction;
import com.minhthien.web.coach.entity.WebsiteFeedback;
import com.minhthien.web.coach.enums.UserRole;
import com.minhthien.web.coach.enums.WalletTransactionType;
import com.minhthien.web.coach.repository.BookingRepository;
import com.minhthien.web.coach.repository.PlatformSettingsRepository;
import com.minhthien.web.coach.repository.UserRepository;
import com.minhthien.web.coach.repository.UserSubscriptionRepository;
import com.minhthien.web.coach.repository.WalletRepository;
import com.minhthien.web.coach.repository.WalletTransactionRepository;
import com.minhthien.web.coach.repository.WebsiteFeedbackRepository;
import com.minhthien.web.coach.service.impl.AdminApiServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminApiServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private WalletTransactionRepository walletTransactionRepository;
    @Mock private WalletRepository walletRepository;
    @Mock private UserSubscriptionRepository userSubscriptionRepository;
    @Mock private PlatformSettingsRepository platformSettingsRepository;
    @Mock private WebsiteFeedbackRepository websiteFeedbackRepository;

    @Test
    void dashboardOverviewSplitsTopUpsAndFeedbackByUserRole() {
        User trainee = user(1L, UserRole.TRAINEES);
        User coach = user(2L, UserRole.COACHES);
        User admin = user(3L, UserRole.ADMIN);
        LocalDateTime createdAt = LocalDateTime.now();

        when(userRepository.findAll()).thenReturn(List.of(trainee, coach, admin));
        when(bookingRepository.findAll()).thenReturn(List.of());
        when(walletTransactionRepository.findAll()).thenReturn(List.of(
                transaction(trainee, WalletTransactionType.TOP_UP, 100_000L, createdAt),
                transaction(trainee, WalletTransactionType.WITHDRAWAL, -20_000L, createdAt),
                transaction(coach, WalletTransactionType.TOP_UP, 250_000L, createdAt),
                transaction(admin, WalletTransactionType.TOP_UP, 999_000L, createdAt)
        ));
        when(websiteFeedbackRepository.findAll()).thenReturn(List.of(
                feedback(trainee, 5),
                feedback(trainee, 3),
                feedback(coach, 4),
                feedback(admin, 1)
        ));

        AdminApiResponses.DashboardOverviewResponse result = service().getDashboardOverview();

        assertThat(result.getTraineeTopUpAmount()).isEqualTo(100_000L);
        assertThat(result.getCoachTopUpAmount()).isEqualTo(250_000L);
        assertThat(result.getTraineeFeedbackCount()).isEqualTo(2L);
        assertThat(result.getTraineeFeedbackAverageRating()).isEqualTo(4.0d);
        assertThat(result.getCoachFeedbackCount()).isEqualTo(1L);
        assertThat(result.getCoachFeedbackAverageRating()).isEqualTo(4.0d);
    }

    @Test
    void dashboardOverviewReturnsZeroFeedbackMetricsWhenNoFeedbackExists() {
        when(userRepository.findAll()).thenReturn(List.of());
        when(bookingRepository.findAll()).thenReturn(List.of());
        when(walletTransactionRepository.findAll()).thenReturn(List.of());
        when(websiteFeedbackRepository.findAll()).thenReturn(List.of());

        AdminApiResponses.DashboardOverviewResponse result = service().getDashboardOverview();

        assertThat(result.getTraineeFeedbackCount()).isZero();
        assertThat(result.getTraineeFeedbackAverageRating()).isZero();
        assertThat(result.getCoachFeedbackCount()).isZero();
        assertThat(result.getCoachFeedbackAverageRating()).isZero();
    }

    private AdminApiServiceImpl service() {
        return new AdminApiServiceImpl(
                userRepository,
                bookingRepository,
                walletTransactionRepository,
                walletRepository,
                userSubscriptionRepository,
                platformSettingsRepository,
                websiteFeedbackRepository
        );
    }

    private User user(Long id, UserRole role) {
        return User.builder()
                .id(id)
                .username("user-" + id)
                .email("user" + id + "@example.com")
                .role(role)
                .build();
    }

    private WalletTransaction transaction(User user, WalletTransactionType type, Long amount, LocalDateTime createdAt) {
        Wallet wallet = Wallet.builder().id(user.getId()).user(user).balance(0L).build();
        return WalletTransaction.builder()
                .wallet(wallet)
                .type(type)
                .amount(amount)
                .balanceBefore(0L)
                .balanceAfter(amount)
                .createdAt(createdAt)
                .build();
    }

    private WebsiteFeedback feedback(User user, Integer rating) {
        return WebsiteFeedback.builder()
                .user(user)
                .rating(rating)
                .build();
    }
}
