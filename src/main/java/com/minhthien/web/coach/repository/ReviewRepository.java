package com.minhthien.web.coach.repository;

import com.minhthien.web.coach.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findTop10ByCoachIdOrderByCreatedAtDesc(Long coachId);
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.coach.id = :coachId")
    Double getAverageRating(Long coachId);

    Long countByCoachId(Long coachId);
}
