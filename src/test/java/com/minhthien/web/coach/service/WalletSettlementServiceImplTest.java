package com.minhthien.web.coach.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhthien.web.coach.dto.response.BookingSettlementResult;
import com.minhthien.web.coach.entity.Booking;
import com.minhthien.web.coach.entity.CoachProfile;
import com.minhthien.web.coach.entity.GymCoach;
import com.minhthien.web.coach.entity.GymProfile;
import com.minhthien.web.coach.entity.PlatformSettings;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.entity.Wallet;
import com.minhthien.web.coach.entity.WalletTransaction;
import com.minhthien.web.coach.enums.GymCoachStatus;
import com.minhthien.web.coach.enums.GymProfileStatus;
import com.minhthien.web.coach.enums.UserRole;
import com.minhthien.web.coach.enums.WalletTransactionType;
import com.minhthien.web.coach.repository.GymCoachRepository;
import com.minhthien.web.coach.repository.PlatformSettingsRepository;
import com.minhthien.web.coach.repository.UserBankAccountRepository;
import com.minhthien.web.coach.repository.UserRepository;
import com.minhthien.web.coach.repository.UserSubscriptionRepository;
import com.minhthien.web.coach.repository.WalletRepository;
import com.minhthien.web.coach.repository.WalletTopUpOrderRepository;
import com.minhthien.web.coach.repository.WalletTransactionRepository;
import com.minhthien.web.coach.service.impl.WalletServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletSettlementServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private WalletRepository walletRepository;
    @Mock private UserBankAccountRepository userBankAccountRepository;
    @Mock private WalletTransactionRepository walletTransactionRepository;
    @Mock private WalletTopUpOrderRepository walletTopUpOrderRepository;
    @Mock private PlatformSettingsRepository platformSettingsRepository;
    @Mock private UserSubscriptionRepository userSubscriptionRepository;
    @Mock private GymCoachRepository gymCoachRepository;
    @Mock private PayOSGatewayService payOSGatewayService;

    @Test
    void settleBookingPaysIndependentCoachWallet() {
        Users users = users();
        Wallet coachWallet = wallet(20L, users.coach, 0L);
        stubCommonSettlement(users, coachWallet, Optional.empty());

        BookingSettlementResult result = service().settleBookingPayment(booking(users));

        assertThat(result.getAdminCommissionAmount()).isEqualTo(20_000L);
        assertThat(result.getCoachPayoutAmount()).isEqualTo(80_000L);
        assertThat(result.getPayoutRecipientUserId()).isEqualTo(users.coach.getId());
        assertThat(result.getPayoutRecipientRole()).isEqualTo(UserRole.COACHES);
        assertThat(result.getGymId()).isNull();
        assertThat(coachWallet.getBalance()).isEqualTo(80_000L);
        assertPayoutDescriptionContains("Payout cho coach Coach One booking #77");
    }

    @Test
    void settleBookingPaysApprovedGymOwnerWallet() {
        Users users = users();
        Wallet ownerWallet = wallet(30L, users.owner, 0L);
        GymProfile gym = gym(users.owner, GymProfileStatus.APPROVED);
        GymCoach gymCoach = GymCoach.builder()
                .id(40L)
                .gym(gym)
                .coach(users.coachProfile)
                .status(GymCoachStatus.ACTIVE)
                .build();
        stubCommonSettlement(users, ownerWallet, Optional.of(gymCoach));

        BookingSettlementResult result = service().settleBookingPayment(booking(users));

        assertThat(result.getCoachPayoutAmount()).isEqualTo(80_000L);
        assertThat(result.getPayoutRecipientUserId()).isEqualTo(users.owner.getId());
        assertThat(result.getPayoutRecipientRole()).isEqualTo(UserRole.GYM_OWNERS);
        assertThat(result.getPayoutRecipientName()).isEqualTo("Fit Hub");
        assertThat(result.getGymId()).isEqualTo(33L);
        assertThat(ownerWallet.getBalance()).isEqualTo(80_000L);
        assertPayoutDescriptionContains("Payout cho phong tap Fit Hub booking #77");
    }

    @Test
    void settleBookingFallsBackToCoachWhenGymIsNotApproved() {
        Users users = users();
        Wallet coachWallet = wallet(20L, users.coach, 0L);
        GymProfile gym = gym(users.owner, GymProfileStatus.SUSPENDED);
        GymCoach gymCoach = GymCoach.builder()
                .id(40L)
                .gym(gym)
                .coach(users.coachProfile)
                .status(GymCoachStatus.ACTIVE)
                .build();
        stubCommonSettlement(users, coachWallet, Optional.of(gymCoach));

        BookingSettlementResult result = service().settleBookingPayment(booking(users));

        assertThat(result.getPayoutRecipientUserId()).isEqualTo(users.coach.getId());
        assertThat(result.getPayoutRecipientRole()).isEqualTo(UserRole.COACHES);
        assertThat(result.getGymId()).isNull();
        assertThat(coachWallet.getBalance()).isEqualTo(80_000L);
    }

    private void stubCommonSettlement(Users users, Wallet providerWallet, Optional<GymCoach> gymCoach) {
        Wallet adminWallet = wallet(99L, users.admin, 0L);
        Wallet traineeWallet = wallet(10L, users.trainee, 200_000L);

        when(platformSettingsRepository.findById(1L)).thenReturn(Optional.of(settings()));
        when(userSubscriptionRepository.findByUserId(users.coach.getId())).thenReturn(Optional.empty());
        when(gymCoachRepository.findFirstByCoachIdAndStatus(users.coachProfile.getId(), GymCoachStatus.ACTIVE))
                .thenReturn(gymCoach);
        when(userRepository.findFirstByRole(UserRole.ADMIN)).thenReturn(Optional.of(users.admin));
        when(walletRepository.findByUserId(providerWallet.getUser().getId())).thenReturn(Optional.of(providerWallet));
        when(walletRepository.findByUserId(users.admin.getId())).thenReturn(Optional.of(adminWallet));
        when(walletRepository.findByUserId(users.trainee.getId())).thenReturn(Optional.of(traineeWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(walletTransactionRepository.save(any(WalletTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void assertPayoutDescriptionContains(String expected) {
        ArgumentCaptor<WalletTransaction> captor = ArgumentCaptor.forClass(WalletTransaction.class);
        org.mockito.Mockito.verify(walletTransactionRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        assertThat(captor.getAllValues())
                .filteredOn(transaction -> transaction.getType() == WalletTransactionType.BOOKING_COACH_PAYOUT)
                .singleElement()
                .extracting(WalletTransaction::getDescription)
                .isEqualTo(expected);
    }

    private WalletServiceImpl service() {
        return new WalletServiceImpl(
                userRepository,
                walletRepository,
                userBankAccountRepository,
                walletTransactionRepository,
                walletTopUpOrderRepository,
                platformSettingsRepository,
                userSubscriptionRepository,
                gymCoachRepository,
                payOSGatewayService,
                new ObjectMapper()
        );
    }

    private Users users() {
        User trainee = user(1L, UserRole.TRAINEES, "Trainee One");
        User coach = user(2L, UserRole.COACHES, "Coach One");
        User owner = user(3L, UserRole.GYM_OWNERS, "Owner One");
        User admin = user(99L, UserRole.ADMIN, "Admin");
        CoachProfile coachProfile = new CoachProfile();
        coachProfile.setId(22L);
        coachProfile.setUser(coach);
        return new Users(trainee, coach, owner, admin, coachProfile);
    }

    private Booking booking(Users users) {
        return Booking.builder()
                .id(77L)
                .trainee(users.trainee)
                .coach(users.coachProfile)
                .price(100_000d)
                .paymentSettled(false)
                .build();
    }

    private User user(Long id, UserRole role, String name) {
        return User.builder()
                .id(id)
                .username("user-" + id)
                .email("user" + id + "@example.com")
                .fullName(name)
                .role(role)
                .build();
    }

    private Wallet wallet(Long id, User user, Long balance) {
        return Wallet.builder()
                .id(id)
                .user(user)
                .balance(balance)
                .currency("VND")
                .build();
    }

    private PlatformSettings settings() {
        return PlatformSettings.builder()
                .id(1L)
                .starterCommissionRate(20)
                .proCoachCommissionRate(12)
                .eliteCoachCommissionRate(0)
                .traineeFreePrice(0L)
                .traineeProPrice(199_000L)
                .traineePremiumPrice(499_000L)
                .coachStarterPrice(0L)
                .coachProPrice(499_000L)
                .coachElitePrice(1_499_000L)
                .build();
    }

    private GymProfile gym(User owner, GymProfileStatus status) {
        return GymProfile.builder()
                .id(33L)
                .owner(owner)
                .name("Fit Hub")
                .status(status)
                .build();
    }

    private record Users(User trainee, User coach, User owner, User admin, CoachProfile coachProfile) {
    }
}
