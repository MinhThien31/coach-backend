package com.minhthien.web.coach.repository;

import com.minhthien.web.coach.entity.CoachVideo;
import com.minhthien.web.coach.enums.VideoType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CoachVideoRepository extends JpaRepository<CoachVideo, Long> {

    List<CoachVideo> findByCoachId(Long coachId);

    @Query("""
SELECT v FROM CoachVideo v
WHERE (:keyword IS NULL OR LOWER(v.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
AND (:type IS NULL OR v.videoType = :type)
AND (:coachId IS NULL OR v.coach.id = :coachId)
""")
    List<CoachVideo> searchVideos(
            String keyword,
            VideoType type,
            Long coachId
    );

    @Query("""
SELECT COALESCE(SUM(v.viewCount),0)
FROM CoachVideo v
WHERE v.coach.id = :coachId
""")
    Long getTotalViewsByCoach(Long coachId);

    long countByCoachId(Long coachId);

    long countByCoachIdAndVideoType(Long coachId, VideoType videoType);
}