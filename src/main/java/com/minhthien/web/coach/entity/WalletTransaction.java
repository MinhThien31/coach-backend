package com.minhthien.web.coach.entity;

import com.minhthien.web.coach.enums.WalletTransactionType;
import com.minhthien.web.coach.enums.WalletWithdrawalStatus;
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

    @Column(length = 50)
    private String bankCode;

    @Column(length = 150)
    private String bankName;

    @Column(length = 50)
    private String bankAccountNumber;

    @Column(length = 150)
    private String bankAccountHolderName;

    @Column(length = 150)
    private String bankBranch;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private WalletWithdrawalStatus withdrawalStatus;

    @Column(length = 255)
    private String adminNote;

    private Long processedByUserId;

    @Column(length = 150)
    private String processedByName;

    private LocalDateTime processedAt;

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
