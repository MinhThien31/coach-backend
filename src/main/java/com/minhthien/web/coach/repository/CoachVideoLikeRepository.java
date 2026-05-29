package com.minhthien.web.coach.repository;

import com.minhthien.web.coach.entity.CoachVideoLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CoachVideoLikeRepository extends JpaRepository<CoachVideoLike, Long> {
    Optional<CoachVideoLike> findByVideoIdAndUserId(Long videoId, Long userId);
    boolean existsByVideoIdAndUserId(Long videoId, Long userId);
    long countByVideoId(Long videoId);
    void deleteByVideoIdAndUserId(Long videoId, Long userId);
    void deleteByVideoId(Long videoId);
}
