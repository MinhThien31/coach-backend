package com.minhthien.web.coach.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "trainee_body_metrics",
        indexes = {
                @Index(name = "idx_body_metrics_trainee_date", columnList = "trainee_id,measuredAt")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraineeBodyMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trainee_id", nullable = false)
    private User trainee;

    @Column(nullable = false)
    private LocalDate measuredAt;

    private Double weight;

    private Double bodyFat;

    private Double muscleMass;

    @Column(length = 500)
    private String note;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (measuredAt == null) {
            measuredAt = LocalDate.now();
        }
    }
}
