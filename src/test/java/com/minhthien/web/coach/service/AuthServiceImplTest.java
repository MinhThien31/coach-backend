package com.minhthien.web.coach.service;

import com.minhthien.web.coach.dto.request.RegisterRequest;
import com.minhthien.web.coach.dto.response.AuthResponse;
import com.minhthien.web.coach.entity.GymProfile;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.entity.Wallet;
import com.minhthien.web.coach.enums.GymProfileStatus;
import com.minhthien.web.coach.enums.UserRole;
import com.minhthien.web.coach.repository.GymProfileRepository;
import com.minhthien.web.coach.repository.PasswordResetOtpRepository;
import com.minhthien.web.coach.repository.TraineeProfileRepository;
import com.minhthien.web.coach.repository.UserRepository;
import com.minhthien.web.coach.repository.WalletRepository;
import com.minhthien.web.coach.security.JwtTokenProvider;
import com.minhthien.web.coach.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private PasswordResetOtpRepository otpRepository;
    @Mock private MailService mailService;
    @Mock private TraineeProfileRepository traineeProfileRepository;
    @Mock private GymProfileRepository gymProfileRepository;
    @Mock private WalletRepository walletRepository;

    @Test
    void registerGymOwnerCreatesWalletAndPendingGymProfile() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("fit-owner");
        request.setFullName("Fit Owner");
        request.setEmail("owner@example.com");
        request.setPhone("0901234567");
        request.setPassword("secret123");
        request.setRole(UserRole.GYM_OWNERS);
        request.setGymName("Fit Hub");
        request.setGymAddress("District 1");
        request.setGymHotline("028123456");
        request.setGymDescription("Partner gym");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(10L);
            return user;
        });
        when(walletRepository.findByUserId(10L)).thenReturn(Optional.empty());
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(gymProfileRepository.findByOwnerId(10L)).thenReturn(Optional.empty());
        when(gymProfileRepository.save(any(GymProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtTokenProvider.generateTokenFromUsername("fit-owner")).thenReturn("jwt-token");

        AuthResponse result = service().register(request);

        assertThat(result.getRole()).isEqualTo(UserRole.GYM_OWNERS);
        assertThat(result.getToken()).isEqualTo("jwt-token");

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(walletCaptor.capture());
        assertThat(walletCaptor.getValue().getUser().getId()).isEqualTo(10L);
        assertThat(walletCaptor.getValue().getBalance()).isZero();

        ArgumentCaptor<GymProfile> gymCaptor = ArgumentCaptor.forClass(GymProfile.class);
        verify(gymProfileRepository).save(gymCaptor.capture());
        assertThat(gymCaptor.getValue().getOwner().getId()).isEqualTo(10L);
        assertThat(gymCaptor.getValue().getName()).isEqualTo("Fit Hub");
        assertThat(gymCaptor.getValue().getAddress()).isEqualTo("District 1");
        assertThat(gymCaptor.getValue().getStatus()).isEqualTo(GymProfileStatus.PENDING);
    }

    private AuthServiceImpl service() {
        return new AuthServiceImpl(
                jwtTokenProvider,
                userRepository,
                passwordEncoder,
                authenticationManager,
                otpRepository,
                mailService,
                traineeProfileRepository,
                gymProfileRepository,
                walletRepository
        );
    }
}
