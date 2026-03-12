package com.minhthien.web.coach.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "platform_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformSettings {

    @Id
    private Long id;

    @Column(nullable = false)
    private Integer starterCommissionRate;

    @Column(nullable = false)
    private Integer proCoachCommissionRate;

    @Column(nullable = false)
    private Integer eliteCoachCommissionRate;

    @Column(nullable = false)
    private Long traineeFreePrice;

    @Column(nullable = false)
    private Long traineeProPrice;

    @Column(nullable = false)
    private Long traineePremiumPrice;

    @Column(nullable = false)
    private Long coachStarterPrice;

    @Column(nullable = false)
    private Long coachProPrice;

    @Column(nullable = false)
    private Long coachElitePrice;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = 1L;
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
