package com.minhthien.web.coach.service;

import com.minhthien.web.coach.dto.request.CoachSearchRequest;
import com.minhthien.web.coach.dto.request.CreateCoachRequest;
import com.minhthien.web.coach.dto.request.UpdateCoachRequest;
import com.minhthien.web.coach.dto.response.CoachDetailResponse;
import com.minhthien.web.coach.dto.response.CoachResponse;
import com.minhthien.web.coach.dto.response.CoachScheduleResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CoachService {
    Page<CoachResponse> searchCoach(CoachSearchRequest request);
    CoachDetailResponse getCoachDetail(Long id);
    List<CoachResponse> getFeaturedCoaches();
    List<CoachResponse> getTrendingCoaches();
    List<CoachScheduleResponse> getCoachSchedule(Long coachId);
    CoachResponse createCoach(CreateCoachRequest request);

    CoachResponse updateCoach(Long id, UpdateCoachRequest request);
}
