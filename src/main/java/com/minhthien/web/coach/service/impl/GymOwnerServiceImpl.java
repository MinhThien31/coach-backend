package com.minhthien.web.coach.service.impl;

import com.minhthien.web.coach.dto.request.GymOwnerRequests;
import com.minhthien.web.coach.dto.response.GymOwnerResponses;
import com.minhthien.web.coach.dto.response.WalletHistoryItemResponse;
import com.minhthien.web.coach.dto.response.WalletResponse;
import com.minhthien.web.coach.entity.Booking;
import com.minhthien.web.coach.entity.CoachProfile;
import com.minhthien.web.coach.entity.GymCoach;
import com.minhthien.web.coach.entity.GymProfile;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.enums.GymCoachStatus;
import com.minhthien.web.coach.enums.GymProfileStatus;
import com.minhthien.web.coach.enums.UserRole;
import com.minhthien.web.coach.exception.BadRequestException;
import com.minhthien.web.coach.exception.ResourceNotFoundException;
import com.minhthien.web.coach.repository.BookingRepository;
import com.minhthien.web.coach.repository.CoachRepository;
import com.minhthien.web.coach.repository.GymCoachRepository;
import com.minhthien.web.coach.repository.GymProfileRepository;
import com.minhthien.web.coach.repository.UserRepository;
import com.minhthien.web.coach.service.GymOwnerService;
import com.minhthien.web.coach.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GymOwnerServiceImpl implements GymOwnerService {

    private final GymProfileRepository gymProfileRepository;
    private final GymCoachRepository gymCoachRepository;
    private final CoachRepository coachRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final WalletService walletService;

    @Override
    @Transactional(readOnly = true)
    public GymOwnerResponses.GymOverviewResponse getOverview(Long ownerUserId) {
        GymProfile gym = getGymByOwner(ownerUserId);
        List<GymOwnerResponses.GymCoachResponse> coaches = getCoaches(ownerUserId);
        List<GymOwnerResponses.GymBookingResponse> bookings = getBookings(ownerUserId);

        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        Long settledRevenue = bookings.stream()
                .filter(b -> Boolean.TRUE.equals(b.getPaymentSettled()))
                .map(GymOwnerResponses.GymBookingResponse::getCoachPayoutAmount)
                .filter(Objects::nonNull)
                .reduce(0L, Long::sum);
        Long monthSettledRevenue = bookings.stream()
                .filter(b -> Boolean.TRUE.equals(b.getPaymentSettled()))
                .filter(b -> b.getStartDate() != null && !b.getStartDate().isBefore(monthStart))
                .map(GymOwnerResponses.GymBookingResponse::getCoachPayoutAmount)
                .filter(Objects::nonNull)
                .reduce(0L, Long::sum);
        Long platformCommission = bookings.stream()
                .filter(b -> Boolean.TRUE.equals(b.getPaymentSettled()))
                .map(GymOwnerResponses.GymBookingResponse::getAdminCommissionAmount)
                .filter(Objects::nonNull)
                .reduce(0L, Long::sum);

        return GymOwnerResponses.GymOverviewResponse.builder()
                .profile(mapProfile(gym))
                .activeCoachCount((long) coaches.size())
                .totalBookingCount((long) bookings.size())
                .monthBookingCount(bookings.stream()
                        .filter(b -> b.getStartDate() != null && !b.getStartDate().isBefore(monthStart))
                        .count())
                .settledRevenue(settledRevenue)
                .monthSettledRevenue(monthSettledRevenue)
                .platformCommission(platformCommission)
                .wallet(walletService.getMyWallet(ownerUserId))
                .coaches(coaches)
                .recentBookings(bookings.stream().limit(8).toList())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GymOwnerResponses.GymProfileResponse getProfile(Long ownerUserId) {
        return mapProfile(getGymByOwner(ownerUserId));
    }

    @Override
    @Transactional
    public GymOwnerResponses.GymProfileResponse updateProfile(Long ownerUserId, GymOwnerRequests.GymProfileUpdateRequest request) {
        GymProfile gym = getGymByOwner(ownerUserId);
        if (StringUtils.hasText(request.getName())) {
            gym.setName(request.getName().trim());
        }
        gym.setAddress(trimToNull(request.getAddress()));
        gym.setHotline(trimToNull(request.getHotline()));
        gym.setDescription(trimToNull(request.getDescription()));
        gym.setLogoUrl(trimToNull(request.getLogoUrl()));
        gym.setCoverUrl(trimToNull(request.getCoverUrl()));
        return mapProfile(gymProfileRepository.save(gym));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GymOwnerResponses.GymCoachResponse> getCoaches(Long ownerUserId) {
        GymProfile gym = getGymByOwner(ownerUserId);
        return gymCoachRepository.findByGymIdAndStatusOrderByJoinedAtDesc(gym.getId(), GymCoachStatus.ACTIVE)
                .stream()
                .map(this::mapCoach)
                .toList();
    }

    @Override
    @Transactional
    public GymOwnerResponses.GymCoachResponse addCoach(Long ownerUserId, GymOwnerRequests.GymCoachAddRequest request) {
        GymProfile gym = getGymByOwner(ownerUserId);
        if (gym.getStatus() != GymProfileStatus.APPROVED) {
            throw new BadRequestException("Phong tap can duoc admin duyet truoc khi them coach");
        }

        CoachProfile coach = resolveCoach(request);
        if (coach.getUser() == null || coach.getUser().getRole() != UserRole.COACHES) {
            throw new BadRequestException("Tai khoan duoc them phai la coach");
        }
        if (gymCoachRepository.existsByCoachIdAndStatus(coach.getId(), GymCoachStatus.ACTIVE)) {
            throw new BadRequestException("Coach nay dang thuoc mot phong tap khac");
        }

        GymCoach gymCoach = gymCoachRepository.save(GymCoach.builder()
                .gym(gym)
                .coach(coach)
                .status(GymCoachStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build());
        return mapCoach(gymCoach);
    }

    @Override
    @Transactional
    public GymOwnerResponses.GymCoachResponse removeCoach(Long ownerUserId, Long coachProfileId) {
        GymProfile gym = getGymByOwner(ownerUserId);
        return removeCoachFromGym(gym.getId(), coachProfileId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GymOwnerResponses.GymBookingResponse> getBookings(Long ownerUserId) {
        GymProfile gym = getGymByOwner(ownerUserId);
        List<Long> coachIds = gymCoachRepository.findByGymIdAndStatusOrderByJoinedAtDesc(gym.getId(), GymCoachStatus.ACTIVE)
                .stream()
                .map(item -> item.getCoach().getId())
                .toList();

        List<Booking> bookings = coachIds.isEmpty()
                ? bookingRepository.findByGymIdOrderByStartDateDescCreatedAtDesc(gym.getId())
                : bookingRepository.findGymBookings(gym.getId(), coachIds);
        return bookings.stream().map(this::mapBooking).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getWallet(Long ownerUserId) {
        ensureGymOwner(ownerUserId);
        return walletService.getMyWallet(ownerUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WalletHistoryItemResponse> getTransactions(Long ownerUserId, int page, int size) {
        ensureGymOwner(ownerUserId);
        return walletService.getMyTransactions(ownerUserId, null, null, null, null, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GymOwnerResponses.GymProfileResponse> getAdminGyms(GymProfileStatus status) {
        return gymProfileRepository.findAll().stream()
                .filter(gym -> status == null || gym.getStatus() == status)
                .map(this::mapProfile)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GymOwnerResponses.GymProfileResponse getAdminGym(Long gymId) {
        return mapProfile(getGym(gymId));
    }

    @Override
    @Transactional
    public GymOwnerResponses.GymProfileResponse updateAdminGymStatus(Long gymId, GymOwnerRequests.AdminGymStatusUpdateRequest request) {
        GymProfile gym = getGym(gymId);
        gym.setStatus(request.getStatus());
        return mapProfile(gymProfileRepository.save(gym));
    }

    @Override
    @Transactional
    public GymOwnerResponses.GymCoachResponse adminRemoveCoach(Long gymId, Long coachProfileId) {
        return removeCoachFromGym(gymId, coachProfileId);
    }

    private GymOwnerResponses.GymCoachResponse removeCoachFromGym(Long gymId, Long coachProfileId) {
        GymCoach gymCoach = gymCoachRepository.findByGymIdAndCoachIdAndStatus(gymId, coachProfileId, GymCoachStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Coach is not active in this gym"));
        gymCoach.setStatus(GymCoachStatus.REMOVED);
        gymCoach.setRemovedAt(LocalDateTime.now());
        return mapCoach(gymCoachRepository.save(gymCoach));
    }

    private GymProfile getGymByOwner(Long ownerUserId) {
        User owner = ensureGymOwner(ownerUserId);
        return gymProfileRepository.findByOwnerId(owner.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Gym profile not found"));
    }

    private GymProfile getGym(Long gymId) {
        return gymProfileRepository.findById(gymId)
                .orElseThrow(() -> new ResourceNotFoundException("Gym profile not found"));
    }

    private User ensureGymOwner(Long ownerUserId) {
        User owner = userRepository.findById(ownerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (owner.getRole() != UserRole.GYM_OWNERS) {
            throw new BadRequestException("Only gym owner can access this resource");
        }
        return owner;
    }

    private CoachProfile resolveCoach(GymOwnerRequests.GymCoachAddRequest request) {
        if (request.getCoachProfileId() != null) {
            return coachRepository.findById(request.getCoachProfileId())
                    .orElseThrow(() -> new ResourceNotFoundException("Coach not found"));
        }
        if (!StringUtils.hasText(request.getEmailOrUsername())) {
            throw new BadRequestException("Coach email, username or id is required");
        }
        String keyword = request.getEmailOrUsername().trim();
        User coachUser = userRepository.findByEmail(keyword)
                .or(() -> userRepository.findByUsername(keyword))
                .orElseThrow(() -> new ResourceNotFoundException("Coach user not found"));
        return coachRepository.findByUserId(coachUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Coach profile not found"));
    }

    private GymOwnerResponses.GymProfileResponse mapProfile(GymProfile gym) {
        User owner = gym.getOwner();
        return GymOwnerResponses.GymProfileResponse.builder()
                .id(gym.getId())
                .ownerId(owner != null ? owner.getId() : null)
                .ownerName(owner != null ? owner.getFullName() : null)
                .ownerEmail(owner != null ? owner.getEmail() : null)
                .name(gym.getName())
                .address(gym.getAddress())
                .hotline(gym.getHotline())
                .description(gym.getDescription())
                .logoUrl(gym.getLogoUrl())
                .coverUrl(gym.getCoverUrl())
                .status(gym.getStatus())
                .createdAt(gym.getCreatedAt())
                .updatedAt(gym.getUpdatedAt())
                .build();
    }

    private GymOwnerResponses.GymCoachResponse mapCoach(GymCoach gymCoach) {
        CoachProfile coach = gymCoach.getCoach();
        User user = coach.getUser();
        return GymOwnerResponses.GymCoachResponse.builder()
                .id(gymCoach.getId())
                .coachProfileId(coach.getId())
                .coachUserId(user != null ? user.getId() : null)
                .coachName(user != null ? user.getFullName() : null)
                .coachEmail(user != null ? user.getEmail() : null)
                .avatarUrl(coach.getAvatarUrl())
                .categoryName(coach.getCategory() != null ? coach.getCategory().getName() : null)
                .price(coach.getPrice())
                .rating(coach.getRating())
                .status(gymCoach.getStatus())
                .joinedAt(gymCoach.getJoinedAt())
                .removedAt(gymCoach.getRemovedAt())
                .build();
    }

    private GymOwnerResponses.GymBookingResponse mapBooking(Booking booking) {
        return GymOwnerResponses.GymBookingResponse.builder()
                .id(booking.getId())
                .coachProfileId(booking.getCoach() != null ? booking.getCoach().getId() : null)
                .coachName(booking.getCoach() != null && booking.getCoach().getUser() != null
                        ? booking.getCoach().getUser().getFullName()
                        : null)
                .traineeName(booking.getTrainee() != null ? booking.getTrainee().getFullName() : null)
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .dayOfWeek(booking.getDayOfWeek())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .price(booking.getPrice())
                .status(booking.getStatus() != null ? booking.getStatus().name() : null)
                .type(booking.getType())
                .paymentSettled(booking.getPaymentSettled())
                .settledAmount(booking.getSettledAmount())
                .adminCommissionAmount(booking.getAdminCommissionAmount())
                .coachPayoutAmount(booking.getCoachPayoutAmount())
                .payoutRecipientUserId(booking.getPayoutRecipientUserId())
                .payoutRecipientRole(booking.getPayoutRecipientRole())
                .payoutRecipientName(booking.getPayoutRecipientName())
                .createdAt(booking.getCreatedAt())
                .settledAt(booking.getSettledAt())
                .build();
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
