package com.minhthien.web.coach.service;

import com.minhthien.web.coach.dto.request.GymOwnerRequests;
import com.minhthien.web.coach.entity.CoachProfile;
import com.minhthien.web.coach.entity.GymProfile;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.enums.GymCoachStatus;
import com.minhthien.web.coach.enums.GymProfileStatus;
import com.minhthien.web.coach.enums.UserRole;
import com.minhthien.web.coach.exception.BadRequestException;
import com.minhthien.web.coach.repository.BookingRepository;
import com.minhthien.web.coach.repository.CoachRepository;
import com.minhthien.web.coach.repository.GymCoachRepository;
import com.minhthien.web.coach.repository.GymProfileRepository;
import com.minhthien.web.coach.repository.UserRepository;
import com.minhthien.web.coach.service.impl.GymOwnerServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GymOwnerServiceImplTest {

    @Mock private GymProfileRepository gymProfileRepository;
    @Mock private GymCoachRepository gymCoachRepository;
    @Mock private CoachRepository coachRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private UserRepository userRepository;
    @Mock private WalletService walletService;

    @Test
    void addCoachRejectsPendingGymProfile() {
        User owner = user(1L, UserRole.GYM_OWNERS);
        GymProfile gym = gym(owner, GymProfileStatus.PENDING);
        GymOwnerRequests.GymCoachAddRequest request = new GymOwnerRequests.GymCoachAddRequest();
        request.setCoachProfileId(20L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(gymProfileRepository.findByOwnerId(1L)).thenReturn(Optional.of(gym));

        assertThatThrownBy(() -> service().addCoach(1L, request))
                .isInstanceOf(BadRequestException.class);
        verify(gymCoachRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void addCoachRejectsCoachAlreadyActiveInAnotherGym() {
        User owner = user(1L, UserRole.GYM_OWNERS);
        User coachUser = user(2L, UserRole.COACHES);
        GymProfile gym = gym(owner, GymProfileStatus.APPROVED);
        CoachProfile coach = new CoachProfile();
        coach.setId(20L);
        coach.setUser(coachUser);
        GymOwnerRequests.GymCoachAddRequest request = new GymOwnerRequests.GymCoachAddRequest();
        request.setCoachProfileId(20L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(gymProfileRepository.findByOwnerId(1L)).thenReturn(Optional.of(gym));
        when(coachRepository.findById(20L)).thenReturn(Optional.of(coach));
        when(gymCoachRepository.existsByCoachIdAndStatus(20L, GymCoachStatus.ACTIVE)).thenReturn(true);

        assertThatThrownBy(() -> service().addCoach(1L, request))
                .isInstanceOf(BadRequestException.class);
        verify(gymCoachRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private GymOwnerServiceImpl service() {
        return new GymOwnerServiceImpl(
                gymProfileRepository,
                gymCoachRepository,
                coachRepository,
                bookingRepository,
                userRepository,
                walletService
        );
    }

    private User user(Long id, UserRole role) {
        return User.builder()
                .id(id)
                .username("user-" + id)
                .email("user" + id + "@example.com")
                .fullName("User " + id)
                .role(role)
                .build();
    }

    private GymProfile gym(User owner, GymProfileStatus status) {
        return GymProfile.builder()
                .id(11L)
                .owner(owner)
                .name("Fit Hub")
                .status(status)
                .build();
    }
}
