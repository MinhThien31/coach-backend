package com.minhthien.web.coach.repository;

import com.minhthien.web.coach.entity.WebsiteFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface WebsiteFeedbackRepository
        extends JpaRepository<WebsiteFeedback, Long>, JpaSpecificationExecutor<WebsiteFeedback> {
    Optional<WebsiteFeedback> findByUserId(Long userId);
}
