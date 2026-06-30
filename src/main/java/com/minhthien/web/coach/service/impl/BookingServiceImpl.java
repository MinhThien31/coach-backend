package com.minhthien.web.coach.service.impl;

import com.minhthien.web.coach.dto.request.BookingRequest;
import com.minhthien.web.coach.dto.response.BookingResponse;
import com.minhthien.web.coach.dto.response.BookingSettlementResult;
import com.minhthien.web.coach.entity.Booking;
import com.minhthien.web.coach.entity.CoachProfile;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.enums.BookingStatus;
import com.minhthien.web.coach.enums.UserRole;
import com.minhthien.web.coach.exception.BadRequestException;
import com.minhthien.web.coach.exception.ResourceNotFoundException;
import com.minhthien.web.coach.exception.UnauthorizedException;
import com.minhthien.web.coach.repository.BookingRepository;
import com.minhthien.web.coach.repository.CoachRepository;
import com.minhthien.web.coach.repository.ScheduleRepository;
import com.minhthien.web.coach.repository.UserRepository;
import com.minhthien.web.coach.service.BookingService;
import com.minhthien.web.coach.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final CoachRepository coachRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final WalletService walletService;

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {

        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User trainee = userRepository
                .findByUsername(username)
                .orElseThrow();

        CoachProfile coach = coachRepository
                .findById(request.getCoachId())
                .orElseThrow(() -> new RuntimeException("Coach not found"));

        ensureFutureBookingTime(request);

        boolean scheduleExists = scheduleRepository.existsBookableSlot(
                coach.getId(),
                request.getDayOfWeek(),
                request.getStartTime(),
                request.getEndTime(),
                request.getStartDate(),
                request.getEndDate()
        );

        if (!scheduleExists) {
            throw new BadRequestException("This time slot is not open for booking");
        }

        boolean exists = bookingRepository
                .existsOverlappingBooking(
                        coach.getId(),
                        request.getDayOfWeek(),
                        request.getStartTime(),
                        request.getEndTime(),
                        request.getStartDate(),
                        request.getEndDate(),
                        List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED)
                );

        if (exists) {
            throw new RuntimeException("This time slot already booked");
        }

        Booking booking = Booking.builder()
                .coach(coach)
                .trainee(trainee)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .price(coach.getPrice())
                .type(request.getType())
                .note(request.getNote())
                .status(BookingStatus.PENDING)
                .traineePaidUpfront(false)
                .paymentSettled(false)
                .createdAt(LocalDateTime.now())
                .build();

        booking = bookingRepository.save(booking);
        
        // Trừ tiền ví học viên ngay khi đặt lịch
        walletService.payForBookingUpfront(booking);
        booking = bookingRepository.save(booking);

        return mapBookingResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> myBookings() {

        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User trainee = userRepository
                .findByUsername(username)
                .orElseThrow();

        return bookingRepository
                .findByTraineeId(trainee.getId())
                .stream()
                .filter(this::isVisibleBooking)
                .map(this::mapBookingResponse)
                .toList();
    }

    @Override
    @Transactional
    public BookingResponse confirmBooking(Long bookingId) {
        User currentUser = getCurrentUser();
        Booking booking = getBooking(bookingId);
        ensureCoachOwnerOrAdmin(currentUser, booking, "confirm");
        ensureStatus(booking, BookingStatus.PENDING, "Only pending booking can be confirmed");

        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
        return mapBookingResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse rejectBooking(Long bookingId) {
        User currentUser = getCurrentUser();
        Booking booking = getBooking(bookingId);
        ensureCoachOwnerOrAdmin(currentUser, booking, "reject");
        ensureStatus(booking, BookingStatus.PENDING, "Only pending booking can be rejected");

        booking.setStatus(BookingStatus.REJECTED);
        walletService.refundBookingPayment(booking, "Hoàn tiền do HLV từ chối yêu cầu đặt lịch");
        bookingRepository.save(booking);
        return mapBookingResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse cancelBookingByCoach(Long bookingId, String reason) {
        User currentUser = getCurrentUser();
        Booking booking = getBooking(bookingId);
        ensureCoachOwnerOrAdmin(currentUser, booking, "cancel");

        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BadRequestException("Only pending or confirmed booking can be cancelled by coach");
        }

        ensureCancellationAllowed(booking, reason);
        applyCancellation(booking, reason, "COACH");
        walletService.refundBookingPayment(booking, "Hoàn tiền do HLV hủy lịch: " + reason);
        bookingRepository.save(booking);
        return mapBookingResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse completeBooking(Long bookingId) {
        User currentUser = getCurrentUser();
        Booking booking = getBooking(bookingId);
        ensureCoachOwnerOrAdmin(currentUser, booking, "complete");
        ensureStatus(booking, BookingStatus.CONFIRMED, "Only confirmed booking can be completed");

        if (!Boolean.TRUE.equals(booking.getPaymentSettled())) {
            BookingSettlementResult settlementResult = walletService.settleBookingPayment(booking);
            booking.setPaymentSettled(true);
            booking.setSettledAmount(settlementResult.getChargedAmount());
            booking.setAdminCommissionAmount(settlementResult.getAdminCommissionAmount());
            booking.setCoachPayoutAmount(settlementResult.getCoachPayoutAmount());
            booking.setPayoutRecipientUserId(settlementResult.getPayoutRecipientUserId());
            booking.setPayoutRecipientRole(settlementResult.getPayoutRecipientRole());
            booking.setPayoutRecipientName(settlementResult.getPayoutRecipientName());
            booking.setGymId(settlementResult.getGymId());
            booking.setSettledAt(LocalDateTime.now());
        }

        booking.setStatus(BookingStatus.COMPLETED);
        bookingRepository.save(booking);
        return mapBookingResponse(booking);
    }

    @Transactional
    @Override
    public void cancelBooking(Long bookingId, String reason) {
        User user = getCurrentUser();
        Booking booking = getBooking(bookingId);

        if (!booking.getTrainee().getId().equals(user.getId())) {
            throw new UnauthorizedException("You cannot cancel this booking");
        }

        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BadRequestException("Only pending or confirmed booking can be cancelled");
        }

        ensureCancellationAllowed(booking, reason);
        applyCancellation(booking, reason, "TRAINEE");
        walletService.refundBookingPayment(booking, "Hoàn tiền do học viên hủy lịch: " + reason);
        bookingRepository.save(booking);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository
                .findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Booking getBooking(Long bookingId) {
        return bookingRepository
                .findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    }

    private void ensureCoachOwnerOrAdmin(User currentUser, Booking booking, String action) {
        if (!booking.getCoach().getUser().getId().equals(currentUser.getId())
                && currentUser.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("You cannot " + action + " this booking");
        }
    }

    private void ensureStatus(Booking booking, BookingStatus expectedStatus, String message) {
        if (booking.getStatus() != expectedStatus) {
            throw new BadRequestException(message);
        }
    }

    private void ensureCancellationAllowed(Booking booking, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new BadRequestException("Cancellation reason is required");
        }

        LocalDateTime sessionStart = LocalDateTime.of(booking.getStartDate(), booking.getStartTime());
        if (LocalDateTime.now().plusHours(24).isAfter(sessionStart)) {
            throw new BadRequestException("Booking can only be cancelled at least 24 hours before start time");
        }
    }

    private void ensureFutureBookingTime(BookingRequest request) {
        LocalDateTime startAt = LocalDateTime.of(request.getStartDate(), request.getStartTime());
        if (!startAt.isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Cannot book a past time slot");
        }
    }

    private void applyCancellation(Booking booking, String reason, String cancelledBy) {
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(reason.trim());
        booking.setCancelledBy(cancelledBy);
        booking.setCancelledAt(LocalDateTime.now());
    }

    private boolean isVisibleBooking(Booking booking) {
        return !LocalDateTime.of(booking.getStartDate(), booking.getEndTime()).isBefore(LocalDateTime.now());
    }

    private BookingResponse mapBookingResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .coachName(booking.getCoach().getUser().getFullName())
                .coachAvatar(booking.getCoach().getAvatarUrl())
                .traineeName(booking.getTrainee().getFullName())
                .traineeAvatar(booking.getTrainee().getAvatarUrl())
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .dayOfWeek(booking.getDayOfWeek())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .price(booking.getPrice())
                .status(booking.getStatus().name())
                .type(booking.getType())
                .note(booking.getNote())
                .paymentSettled(booking.getPaymentSettled())
                .settledAmount(booking.getSettledAmount())
                .adminCommissionAmount(booking.getAdminCommissionAmount())
                .coachPayoutAmount(booking.getCoachPayoutAmount())
                .cancellationReason(booking.getCancellationReason())
                .cancelledBy(booking.getCancelledBy())
                .cancelledAt(booking.getCancelledAt())
                .build();
    }
}
