package com.minhthien.web.coach.repository;

import com.minhthien.web.coach.entity.UserBankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserBankAccountRepository extends JpaRepository<UserBankAccount, Long> {
    Optional<UserBankAccount> findByUserId(Long userId);
}
