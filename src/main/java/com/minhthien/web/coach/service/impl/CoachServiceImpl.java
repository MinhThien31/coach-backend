package com.minhthien.web.coach.service.impl;

import com.minhthien.web.coach.dto.request.CoachSearchRequest;
import com.minhthien.web.coach.dto.request.CreateCoachRequest;
import com.minhthien.web.coach.dto.request.UpdateCoachRequest;
import com.minhthien.web.coach.dto.response.CoachDetailResponse;
import com.minhthien.web.coach.dto.response.CoachResponse;
import com.minhthien.web.coach.dto.response.CoachScheduleResponse;
import com.minhthien.web.coach.dto.response.ReviewResponse;
import com.minhthien.web.coach.entity.*;
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
import org.springframework.web.multipart.MultipartFile;

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

    @Override
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
                .map(s -> CoachScheduleResponse.builder()
                        .dayOfWeek(s.getDayOfWeek())
                        .startTime(s.getStartTime())
                        .endTime(s.getEndTime())
                        .build())
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

        return CoachDetailResponse.builder()
                .id(coach.getId())
                .fullName(coach.getUser().getFullName())
                .avatar(coach.getAvatarUrl())
                .category(coach.getCategory().getName())
                .location(coach.getLocation())
                .price(coach.getPrice())
                .rating(coach.getRating())
                .students(coach.getStudents())
                .totalSessions(coach.getTotalSessions())
                .responseRate(coach.getResponseRate())
                .bio(coach.getBio())
                .specializations(specializations)
                .certificates(certificates)
                .schedules(schedules)
                .reviews(reviews)
                .build();
    }

    @Override
    public List<CoachResponse> getFeaturedCoaches() {

        return coachRepository
                .findTop6ByOrderByRatingDesc()
                .stream()
                .map(c -> CoachResponse.builder()
                        .id(c.getId())
                        .fullName(c.getUser().getFullName())
                        .avatar(c.getAvatarUrl())
                        .category(c.getCategory().getName())
                        .price(c.getPrice())
                        .rating(c.getRating())
                        .reviewCount(c.getReviewCount())
                        .location(c.getLocation())
                        .build())
                .toList();
    }

    @Override
    public List<CoachResponse> getTrendingCoaches() {

        return coachRepository
                .findTop6ByOrderByStudentsDesc()
                .stream()
                .map(c -> CoachResponse.builder()
                        .id(c.getId())
                        .fullName(c.getUser().getFullName())
                        .avatar(c.getAvatarUrl())
                        .category(c.getCategory().getName())
                        .price(c.getPrice())
                        .rating(c.getRating())
                        .reviewCount(c.getReviewCount())
                        .location(c.getLocation())
                        .build())
                .toList();
    }

    @Override
    public List<CoachScheduleResponse> getCoachSchedule(Long coachId) {

        return scheduleRepository
                .findByCoachId(coachId)
                .stream()
                .map(s -> CoachScheduleResponse.builder()
                        .dayOfWeek(s.getDayOfWeek())
                        .startTime(s.getStartTime())
                        .endTime(s.getEndTime())
                        .build())
                .toList();
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
                .orElseThrow(() -> new RuntimeException("Category not found"));

        String avatarUrl = imageService.upload(request.getAvatar());

        CoachProfile coach = new CoachProfile();

        coach.setUser(user);
        coach.setCategory(category);
        coach.setPrice(request.getPrice());
        coach.setExperienceYears(request.getExperienceYears());
        coach.setBio(request.getBio());
        coach.setAvatarUrl(avatarUrl);
        coach.setLocation(request.getLocation());

        coach.setRating(0.0);
        coach.setReviewCount(0);
        coach.setStudents(0);
        coach.setTotalSessions(0);
        coach.setResponseRate(100);

        coachRepository.save(coach);

        return CoachResponse.builder()
                .id(coach.getId())
                .fullName(user.getFullName())
                .avatar(avatarUrl)
                .category(category.getName())
                .price(coach.getPrice())
                .rating(coach.getRating())
                .reviewCount(coach.getReviewCount())
                .location(coach.getLocation())
                .build();
    }

    @Override
    public CoachResponse updateCoach(Long id, UpdateCoachRequest request) {

        CoachProfile coach = coachRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Coach not found"));

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

        coachRepository.save(coach);

        return CoachResponse.builder()
                .id(coach.getId())
                .fullName(coach.getUser().getFullName())
                .avatar(coach.getAvatarUrl())
                .category(coach.getCategory().getName())
                .price(coach.getPrice())
                .rating(coach.getRating())
                .reviewCount(coach.getReviewCount())
                .location(coach.getLocation())
                .build();
    }
}
