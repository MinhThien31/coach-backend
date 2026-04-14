package com.minhthien.web.coach.entity;

import com.minhthien.web.coach.enums.WalletTransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private WalletTransactionType type;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    private Long balanceBefore;

    @Column(nullable = false)
    private Long balanceAfter;

    @Column(length = 255)
    private String description;

    @Column(length = 50)
    private String referenceType;

    @Column(length = 100)
    private String referenceId;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
