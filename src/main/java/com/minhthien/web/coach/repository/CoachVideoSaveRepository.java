package com.minhthien.web.coach.repository;

import com.minhthien.web.coach.entity.CoachVideoSave;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CoachVideoSaveRepository extends JpaRepository<CoachVideoSave, Long> {
    Optional<CoachVideoSave> findByVideoIdAndUserId(Long videoId, Long userId);
    boolean existsByVideoIdAndUserId(Long videoId, Long userId);
    long countByVideoId(Long videoId);
    List<CoachVideoSave> findByUserIdOrderByCreatedAtDesc(Long userId);
    void deleteByVideoIdAndUserId(Long videoId, Long userId);
    void deleteByVideoId(Long videoId);
}
