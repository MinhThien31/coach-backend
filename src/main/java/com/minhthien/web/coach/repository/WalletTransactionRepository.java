package com.minhthien.web.coach.repository;

import com.minhthien.web.coach.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    List<WalletTransaction> findTop50ByWalletIdOrderByCreatedAtDesc(Long walletId);
    long countByWalletId(Long walletId);
}
