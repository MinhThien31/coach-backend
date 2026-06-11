package com.minhthien.web.coach.service;

import com.minhthien.web.coach.dto.request.WebsiteFeedbackRequest;
import com.minhthien.web.coach.dto.response.WebsiteFeedbackResponse;
import com.minhthien.web.coach.enums.UserRole;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

public interface WebsiteFeedbackService {
    WebsiteFeedbackResponse getMine(Long userId);

    WebsiteFeedbackResponse saveMine(Long userId, WebsiteFeedbackRequest request);

    Page<WebsiteFeedbackResponse> getAll(
            String keyword,
            Integer rating,
            UserRole role,
            LocalDate from,
            LocalDate to,
            int page,
            int size
    );
}
