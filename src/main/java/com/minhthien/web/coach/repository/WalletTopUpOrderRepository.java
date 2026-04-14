package com.minhthien.web.coach.repository;

import com.minhthien.web.coach.entity.WalletTopUpOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletTopUpOrderRepository extends JpaRepository<WalletTopUpOrder, Long> {
    Optional<WalletTopUpOrder> findByOrderCode(Long orderCode);
    Optional<WalletTopUpOrder> findByOrderCodeAndUserId(Long orderCode, Long userId);
    boolean existsByOrderCode(Long orderCode);
}
