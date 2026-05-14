package com.minhthien.web.coach.repository;

import com.minhthien.web.coach.entity.CoachProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoachRepository extends JpaRepository<CoachProfile, Long>,
        JpaSpecificationExecutor<CoachProfile> {
    @Override
    @EntityGraph(attributePaths = {"user", "category"})
    Page<CoachProfile> findAll(Specification<CoachProfile> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "category"})
    List<CoachProfile> findTop6ByOrderByRatingDesc();

    @EntityGraph(attributePaths = {"user", "category"})
    List<CoachProfile> findTop6ByOrderByStudentsDesc();

    @EntityGraph(attributePaths = {"user", "category"})
    Optional<CoachProfile> findById(Long id);

    Optional<CoachProfile> findByUserId(Long userId);
}
