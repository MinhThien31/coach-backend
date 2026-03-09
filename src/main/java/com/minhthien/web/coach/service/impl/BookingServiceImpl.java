package com.minhthien.web.coach.service.impl;

import com.minhthien.web.coach.dto.request.BookingRequest;
import com.minhthien.web.coach.dto.response.BookingResponse;
import com.minhthien.web.coach.entity.Booking;
import com.minhthien.web.coach.entity.CoachProfile;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.enums.BookingStatus;
import com.minhthien.web.coach.repository.BookingRepository;
import com.minhthien.web.coach.repository.CoachRepository;
import com.minhthien.web.coach.repository.UserRepository;
import com.minhthien.web.coach.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final CoachRepository coachRepository;
    private final UserRepository userRepository;

    @Override
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

        Booking booking = Booking.builder()
                .coach(coach)
                .trainee(trainee)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .price(coach.getPrice())
                .status(BookingStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        bookingRepository.save(booking);

        return BookingResponse.builder()
                .id(booking.getId())
                .coachName(coach.getUser().getFullName())
                .traineeName(trainee.getFullName())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .price(booking.getPrice())
                .status(booking.getStatus().name())
                .build();
    }



    @Override
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
                .map(b -> BookingResponse.builder()
                        .id(b.getId())
                        .coachName(b.getCoach().getUser().getFullName())
                        .traineeName(trainee.getFullName())
                        .startTime(b.getStartTime())
                        .endTime(b.getEndTime())
                        .price(b.getPrice())
                        .status(b.getStatus().name())
                        .build())
                .toList();
    }

    @Override
    public void cancelBooking(Long bookingId) {

        Booking booking = bookingRepository
                .findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setStatus(BookingStatus.CANCELLED);

        bookingRepository.save(booking);
    }
}
