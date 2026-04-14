package com.minhthien.web.coach.repository;

import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    List<User> findByRole(UserRole role);
    Optional<User> findFirstByRole(UserRole role);
}
