package com.minhthien.web.coach.entity;

import com.minhthien.web.coach.enums.WalletTopUpOrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_top_up_orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTopUpOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(nullable = false, unique = true)
    private Long orderCode;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false, length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private WalletTopUpOrderStatus status = WalletTopUpOrderStatus.PENDING;

    @Column(length = 100)
    private String paymentLinkId;

    @Column(length = 500)
    private String checkoutUrl;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String qrCode;

    @Column(length = 100)
    private String payosReference;

    @Column(length = 50)
    private String payosCode;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String rawWebhookPayload;

    private LocalDateTime paidAt;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = WalletTopUpOrderStatus.PENDING;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
