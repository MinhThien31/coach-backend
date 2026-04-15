package com.minhthien.web.coach.repository;

import com.minhthien.web.coach.entity.WalletTransaction;
import com.minhthien.web.coach.enums.WalletTransactionType;
import com.minhthien.web.coach.enums.WalletWithdrawalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    List<WalletTransaction> findTop50ByWalletIdOrderByCreatedAtDesc(Long walletId);
    List<WalletTransaction> findTop100ByWalletIdAndTypeOrderByCreatedAtDesc(Long walletId, WalletTransactionType type);
    long countByWalletId(Long walletId);
    List<WalletTransaction> findTop100ByTypeOrderByCreatedAtDesc(WalletTransactionType type);
    List<WalletTransaction> findTop100ByTypeAndWithdrawalStatusOrderByCreatedAtDesc(
            WalletTransactionType type,
            WalletWithdrawalStatus withdrawalStatus
    );
}
