package com.minhthien.web.coach.repository;

import com.minhthien.web.coach.entity.GymProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GymProfileRepository extends JpaRepository<GymProfile, Long> {
    @EntityGraph(attributePaths = {"owner"})
    Optional<GymProfile> findByOwnerId(Long ownerId);
}
