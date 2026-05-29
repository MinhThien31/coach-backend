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
        name = "trainee_exercise_progress",
        indexes = {
                @Index(name = "idx_exercise_progress_trainee_date", columnList = "trainee_id,measuredAt"),
                @Index(name = "idx_exercise_progress_trainee_exercise", columnList = "trainee_id,exerciseName")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraineeExerciseProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trainee_id", nullable = false)
    private User trainee;

    @Column(nullable = false, length = 120)
    private String exerciseName;

    @Column(nullable = false)
    private LocalDate measuredAt;

    @Column(nullable = false)
    private Double value;

    @Column(nullable = false, length = 30)
    private String unit;

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
