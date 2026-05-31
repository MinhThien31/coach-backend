package com.minhthien.web.coach.service.impl;

import com.minhthien.web.coach.dto.request.*;
import com.minhthien.web.coach.dto.response.*;
import com.minhthien.web.coach.entity.*;
import com.minhthien.web.coach.enums.BookingStatus;
import com.minhthien.web.coach.enums.UserRole;
import com.minhthien.web.coach.exception.BadRequestException;
import com.minhthien.web.coach.exception.ResourceNotFoundException;
import com.minhthien.web.coach.exception.UnauthorizedException;
import com.minhthien.web.coach.repository.*;
import com.minhthien.web.coach.service.CoachService;
import com.minhthien.web.coach.service.ImageService;
import com.minhthien.web.coach.specification.CoachSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CoachServiceImpl implements CoachService {

    private final CoachRepository coachRepository;
    private final ScheduleRepository scheduleRepository;
    private final ReviewRepository reviewRepository;
    private final CoachSpecializationRepository specializationRepository;
    private final CoachCertificateRepository certificateRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final CategoryRepository categoryRepository;
    private final BookingRepository bookingRepository;


    @Override
    @Transactional(readOnly = true)
    public Page<CoachResponse> searchCoach(CoachSearchRequest request) {

        Sort sort;

        if ("priceAsc".equals(request.getSort())) {

            sort = Sort.by("price").ascending();

        } else if ("priceDesc".equals(request.getSort())) {

            sort = Sort.by("price").descending();

        } else if ("rating".equals(request.getSort())) {

            sort = Sort.by("rating").descending();

        } else {

            sort = Sort.by("rating").descending();
        }

        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                sort
        );

        Page<CoachProfile> coaches = coachRepository.findAll(
                CoachSpecification.filter(request),
                pageable
        );

        return coaches.map(coach ->
                CoachResponse.builder()
                        .id(coach.getId())
                        .userId(coach.getUser().getId())
                        .fullName(coach.getUser().getFullName())
                        .avatar(coach.getAvatarUrl())
                        .category(coach.getCategory().getName())
                        .price(coach.getPrice())
                        .rating(coach.getRating())
                        .reviewCount(coach.getReviewCount())
                        .location(coach.getLocation())
                        .build()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CoachDetailResponse getCoachDetail(Long id) {

        CoachProfile coach = coachRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coach not found"));

        List<String> specializations = specializationRepository
                .findByCoachId(id)
                .stream()
                .map(CoachSpecialization::getName)
                .toList();

        List<String> certificates = certificateRepository
                .findByCoachId(id)
                .stream()
                .map(CoachCertificate::getName)
                .toList();

        List<CoachScheduleResponse> schedules = scheduleRepository
                .findByCoachId(id)
                .stream()
                .map(s -> mapCoachScheduleResponse(s, null, null))
                .toList();

        List<ReviewResponse> reviews = reviewRepository
                .findTop10ByCoachIdOrderByCreatedAtDesc(id)
                .stream()
                .map(r -> ReviewResponse.builder()
                        .userName(r.getUser().getFullName())
                        .avatar(coach.getAvatarUrl())
                        .rating(r.getRating())
                        .comment(r.getComment())
                        .createdAt(r.getCreatedAt())
                        .build())
                .toList();

        Double rating = reviewRepository.getAverageRating(id);
        if (rating == null) {
            rating = 0.0;
        }
        Long students = bookingRepository.countStudentsByCoach(id);
        if (students == null) students = 0L;

        Long sessions = bookingRepository.countSessionsByCoach(id);
        if (sessions == null) sessions = 0L;

        return CoachDetailResponse.builder()
                .id(coach.getId())
                .userId(coach.getUser().getId())
                .fullName(coach.getUser().getFullName())
                .avatar(coach.getAvatarUrl())
                .category(coach.getCategory().getName())
                .location(coach.getLocation())
                .price(coach.getPrice())
                .teachingType(coach.getTeachingType().name())
                .rating(rating != null ? rating : 0)
                .students(students != null ? students : 0)
                .totalSessions(sessions != null ? sessions : 0)
                .responseRate(coach.getResponseRate())
                .bio(coach.getBio())
                .specializations(specializations)
                .certificates(certificates)
                .schedules(schedules)
                .reviews(reviews)
                .build();
    }

    @Override
    public ReviewResponse createReview(CreateReviewRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CoachProfile coach = coachRepository.findById(request.getCoachId())
                .orElseThrow(() -> new RuntimeException("Coach not found"));

        Review review = Review.builder()
                .coach(coach)
                .user(user)
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(LocalDateTime.now())
                .build();



        reviewRepository.save(review);

        Double avgRating = reviewRepository.getAverageRating(coach.getId());
        Long reviewCount = reviewRepository.countByCoachId(coach.getId());

        coach.setRating(avgRating != null ? avgRating : 0);
        coach.setReviewCount(reviewCount != null ? reviewCount.intValue() : 0);

        coachRepository.save(coach);
        return ReviewResponse.builder()
                .userName(user.getFullName())
                .avatar(user.getAvatarUrl())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }

    @Override
    public SpecializationResponse create(CreateSpecializationRequest request) {
        CoachProfile coach = coachRepository.findById(request.getCoachId())
                .orElseThrow();

        CoachSpecialization specialization = CoachSpecialization.builder()
                .coach(coach)
                .name(request.getName())
                .build();

        specializationRepository.save(specialization);

        return SpecializationResponse.builder()
                .id(specialization.getId())
                .name(specialization.getName())
                .build();
    }

    @Override
    public CertificateResponse createCertificate(CreateCertificateRequest request) {
        CoachProfile coach = coachRepository.findById(request.getCoachId())
                .orElseThrow();
        CoachCertificate certificate =CoachCertificate.builder()
                .coach(coach)
                .name(request.getName())
                .build();
        certificateRepository.save(certificate);

        return CertificateResponse.builder()
                .id(certificate.getId())
                .name(certificate.getName())
                .build();
    }

    @Override
    public ScheduleResponse createSchedule(CreateScheduleRequest request) {

        ensureFutureSchedule(request);

        CoachProfile coach = coachRepository.findById(request.getCoachId())
                .orElseThrow(() -> new RuntimeException("Coach not found"));

        CoachSchedule schedule = CoachSchedule.builder()
                .coach(coach)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .dayOfWeek(DayOfWeek.valueOf(request.getDayOfWeek()))
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        scheduleRepository.save(schedule);

        return ScheduleResponse.builder()
                .id(schedule.getId())
                .dayOfWeek(schedule.getDayOfWeek().name())
                .startTime(schedule.getStartTime().toString())
                .endTime(schedule.getEndTime().toString())
                .build();
    }
    @Override
    @Transactional(readOnly = true)
    public List<CoachResponse> getFeaturedCoaches() {

        return coachRepository
                .findTop6ByOrderByRatingDesc()
                .stream()
                .map(c -> CoachResponse.builder()
                        .id(c.getId())
                        .userId(c.getUser().getId())
                        .fullName(c.getUser().getFullName())
                        .avatar(c.getAvatarUrl())
                        .category(c.getCategory().getName())
                        .price(c.getPrice())
                        .rating(c.getRating())
                        .reviewCount(c.getReviewCount())
                        .location(c.getLocation())
                        .bio(c.getBio())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoachResponse> getTrendingCoaches() {

        return coachRepository
                .findTop6ByOrderByStudentsDesc()
                .stream()
                .map(c -> CoachResponse.builder()
                        .id(c.getId())
                        .userId(c.getUser().getId())
                        .fullName(c.getUser().getFullName())
                        .avatar(c.getAvatarUrl())
                        .category(c.getCategory().getName())
                        .price(c.getPrice())
                        .rating(c.getRating())
                        .reviewCount(c.getReviewCount())
                        .location(c.getLocation())
                        .bio(c.getBio())
                        .teachingType(c.getTeachingType())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoachScheduleResponse> getCoachSchedule(Long coachId, LocalDate startDate, LocalDate endDate) {

        return scheduleRepository
                .findByCoachId(coachId)
                .stream()
                .filter(s -> matchesRequestedRange(s, startDate, endDate))
                .filter(this::isVisibleSchedule)
                .map(s -> mapCoachScheduleResponse(s, startDate, endDate))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoachScheduleResponse> getAvailableSlots(Long coachId, LocalDate date) {
        if (date == null) {
            throw new BadRequestException("date is required");
        }

        return scheduleRepository
                .findByCoachId(coachId)
                .stream()
                .filter(s -> !s.getStartDate().isAfter(date)
                        && !s.getEndDate().isBefore(date)
                        && s.getDayOfWeek() == date.getDayOfWeek())
                .filter(this::isVisibleSchedule)
                .map(s -> mapCoachScheduleResponse(s, date, date))
                .filter(CoachScheduleResponse::getAvailable)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoachScheduleResponse> getScheduleWithAvailability(Long coachId) {
        return getCoachSchedule(coachId, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public CoachResponse getMyCoachProfile(Long currentUserId) {
        CoachProfile coach = coachRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Coach profile not found"));
        return mapCoachResponse(coach);
    }

    private CoachScheduleResponse mapCoachScheduleResponse(
            CoachSchedule schedule,
            LocalDate requestedStartDate,
            LocalDate requestedEndDate
    ) {
        LocalDate checkStartDate;
        LocalDate checkEndDate;

        if (requestedStartDate == null && requestedEndDate == null) {
            checkStartDate = schedule.getStartDate();
            checkEndDate = schedule.getEndDate();
        } else {
            checkStartDate = requestedStartDate != null ? requestedStartDate : requestedEndDate;
            checkEndDate = requestedEndDate != null ? requestedEndDate : checkStartDate;
        }

        List<Booking> bookings = bookingRepository.findOverlappingBookings(
                schedule.getCoach().getId(),
                schedule.getDayOfWeek(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                checkStartDate,
                checkEndDate,
                List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED)
        );

        Booking booking = bookings.isEmpty() ? null : bookings.get(0);

        return CoachScheduleResponse.builder()
                .id(schedule.getId())
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .dayOfWeek(schedule.getDayOfWeek().name())
                .startTime(schedule.getStartTime().toString())
                .endTime(schedule.getEndTime().toString())
                .available(booking == null)
                .status(booking == null ? "AVAILABLE" : "BOOKED")
                .bookingId(booking == null ? null : booking.getId())
                .bookingStatus(booking == null ? null : booking.getStatus().name())
                .build();
    }

    private boolean matchesRequestedRange(CoachSchedule schedule, LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return true;
        }

        LocalDate rangeStart = startDate != null ? startDate : endDate;
        LocalDate rangeEnd = endDate != null ? endDate : rangeStart;

        boolean overlapsScheduleRange = !schedule.getStartDate().isAfter(rangeEnd)
                && !schedule.getEndDate().isBefore(rangeStart);

        return overlapsScheduleRange && rangeContainsDayOfWeek(rangeStart, rangeEnd, schedule.getDayOfWeek());
    }

    private boolean rangeContainsDayOfWeek(LocalDate startDate, LocalDate endDate, DayOfWeek dayOfWeek) {
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (date.getDayOfWeek() == dayOfWeek) {
                return true;
            }
        }

        return false;
    }

    private boolean isVisibleSchedule(CoachSchedule schedule) {
        LocalDate today = LocalDate.now();
        if (schedule.getEndDate().isBefore(today)) {
            return false;
        }
        return !schedule.getEndDate().isEqual(today)
                || schedule.getEndTime().isAfter(LocalTime.now());
    }

    private void ensureFutureSchedule(CreateScheduleRequest request) {
        if (request.getEndTime() != null && request.getStartTime() != null
                && !request.getEndTime().isAfter(request.getStartTime())) {
            throw new BadRequestException("Schedule end time must be after start time");
        }

        if (request.getStartDate() != null && request.getStartTime() != null) {
            LocalDateTime startAt = LocalDateTime.of(request.getStartDate(), request.getStartTime());
            if (!startAt.isAfter(LocalDateTime.now())) {
                throw new BadRequestException("Cannot create schedule in the past");
            }
        }
    }

    @Override
    public CoachResponse createCoach(CreateCoachRequest request) {

        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository
                .findByUsername(username)
                .orElseThrow();

        Category category = categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        String avatarUrl = imageService.upload(request.getAvatar());

        CoachProfile coach = new CoachProfile();

        coach.setUser(user);
        coach.setCategory(category);
        coach.setPrice(request.getPrice());
        coach.setExperienceYears(request.getExperienceYears());
        coach.setBio(request.getBio());
        coach.setAvatarUrl(avatarUrl);
        coach.setLocation(request.getLocation());
        coach.setTeachingType(request.getTeachingType());

        coach.setRating(0.0);
        coach.setReviewCount(0);
        coach.setStudents(0);
        coach.setTotalSessions(0);
        coach.setResponseRate(100);

        coachRepository.save(coach);

        return CoachResponse.builder()
                .id(coach.getId())
                .userId(user.getId())
                .fullName(user.getFullName())
                .avatar(avatarUrl)
                .category(category.getName())
                .price(coach.getPrice())
                .rating(coach.getRating())
                .bio(coach.getBio())
                .reviewCount(coach.getReviewCount())
                .location(coach.getLocation())
                .teachingType(coach.getTeachingType())
                .build();
    }

    @Override
    public CoachResponse updateCoach(Long id, UpdateCoachRequest request) {

        CoachProfile coach = coachRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Coach not found"));
        ensureCoachOwnerOrAdmin(coach);

        if (request.getCategoryId() != null) {

            Category category = categoryRepository
                    .findById(request.getCategoryId())
                    .orElseThrow();

            coach.setCategory(category);
        }

        if (request.getPrice() != null) {
            coach.setPrice(request.getPrice());
        }

        if (request.getExperienceYears() != null) {
            coach.setExperienceYears(request.getExperienceYears());
        }

        if (request.getBio() != null) {
            coach.setBio(request.getBio());
        }
        if (request.getLocation() != null) {
            coach.setLocation(request.getLocation());
        }

        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {

            String avatarUrl = imageService.upload(request.getAvatar());

            coach.setAvatarUrl(avatarUrl);
        }
        if (request.getTeachingType() != null) {
            coach.setTeachingType(request.getTeachingType());
        }

        coachRepository.save(coach);

        return CoachResponse.builder()
                .id(coach.getId())
                .userId(coach.getUser().getId())
                .fullName(coach.getUser().getFullName())
                .avatar(coach.getAvatarUrl())
                .category(coach.getCategory().getName())
                .price(coach.getPrice())
                .rating(coach.getRating())
                .reviewCount(coach.getReviewCount())
                .location(coach.getLocation())
                .teachingType(coach.getTeachingType())
                .build();
    }

    @Override
    @Transactional
    public CoachResponse updateMyCoachProfile(Long currentUserId, UpdateCoachRequest request) {
        CoachProfile coach = coachRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Coach profile not found"));
        return updateCoach(coach.getId(), request);
    }

    @Override
    @Transactional
    public ScheduleResponse updateSchedule(Long currentUserId, Long scheduleId, CreateScheduleRequest request) {
        CoachSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));
        ensureScheduleOwnerOrAdmin(currentUserId, schedule);
        ensureFutureSchedule(request);

        if (hasActiveBooking(schedule)) {
            throw new BadRequestException("Cannot update schedule with pending or confirmed bookings");
        }

        if (request.getStartDate() != null) {
            schedule.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            schedule.setEndDate(request.getEndDate());
        }
        if (request.getDayOfWeek() != null) {
            schedule.setDayOfWeek(DayOfWeek.valueOf(request.getDayOfWeek()));
        }
        if (request.getStartTime() != null) {
            schedule.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            schedule.setEndTime(request.getEndTime());
        }

        scheduleRepository.save(schedule);
        return mapScheduleResponse(schedule);
    }

    @Override
    @Transactional
    public void deleteSchedule(Long currentUserId, Long scheduleId) {
        CoachSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));
        ensureScheduleOwnerOrAdmin(currentUserId, schedule);

        if (hasActiveBooking(schedule)) {
            throw new BadRequestException("Cannot delete schedule with pending or confirmed bookings");
        }

        scheduleRepository.delete(schedule);
    }

    private void ensureScheduleOwnerOrAdmin(Long currentUserId, CoachSchedule schedule) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!schedule.getCoach().getUser().getId().equals(currentUserId)
                && currentUser.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("You cannot manage this schedule");
        }
    }

    private void ensureCoachOwnerOrAdmin(CoachProfile coach) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!coach.getUser().getId().equals(currentUser.getId()) && currentUser.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("You cannot update this coach profile");
        }
    }

    private boolean hasActiveBooking(CoachSchedule schedule) {
        return !bookingRepository.findOverlappingBookings(
                schedule.getCoach().getId(),
                schedule.getDayOfWeek(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getStartDate(),
                schedule.getEndDate(),
                List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED)
        ).isEmpty();
    }

    private ScheduleResponse mapScheduleResponse(CoachSchedule schedule) {
        return ScheduleResponse.builder()
                .id(schedule.getId())
                .dayOfWeek(schedule.getDayOfWeek().name())
                .startTime(schedule.getStartTime().toString())
                .endTime(schedule.getEndTime().toString())
                .build();
    }

    private CoachResponse mapCoachResponse(CoachProfile coach) {
        return CoachResponse.builder()
                .id(coach.getId())
                .userId(coach.getUser().getId())
                .fullName(coach.getUser().getFullName())
                .avatar(coach.getAvatarUrl())
                .category(coach.getCategory() == null ? null : coach.getCategory().getName())
                .price(coach.getPrice())
                .rating(coach.getRating())
                .reviewCount(coach.getReviewCount())
                .bio(coach.getBio())
                .location(coach.getLocation())
                .teachingType(coach.getTeachingType())
                .build();
    }
}
