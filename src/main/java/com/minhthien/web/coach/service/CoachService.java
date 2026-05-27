package com.minhthien.web.coach.service;

import com.minhthien.web.coach.dto.request.*;
import com.minhthien.web.coach.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface CoachService {
    Page<CoachResponse> searchCoach(CoachSearchRequest request);
    CoachDetailResponse getCoachDetail(Long id);
    ReviewResponse createReview(CreateReviewRequest request);
    SpecializationResponse create(CreateSpecializationRequest request);
    CertificateResponse createCertificate(CreateCertificateRequest request);
    ScheduleResponse createSchedule(CreateScheduleRequest request);
    List<CoachResponse> getFeaturedCoaches();
    List<CoachResponse> getTrendingCoaches();
    List<CoachScheduleResponse> getCoachSchedule(Long coachId, LocalDate startDate, LocalDate endDate);
    CoachResponse createCoach(CreateCoachRequest request);

    CoachResponse updateCoach(Long id, UpdateCoachRequest request);
}
