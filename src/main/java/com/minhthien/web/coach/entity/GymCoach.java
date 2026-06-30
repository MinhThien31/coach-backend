package com.minhthien.web.coach.entity;

import com.minhthien.web.coach.enums.GymCoachStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "gym_coaches",
        indexes = {
                @Index(name = "idx_gym_coaches_gym_status", columnList = "gym_id,status"),
                @Index(name = "idx_gym_coaches_coach_status", columnList = "coach_id,status")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GymCoach {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_id", nullable = false)
    private GymProfile gym;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = false)
    private CoachProfile coach;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GymCoachStatus status;

    private LocalDateTime joinedAt;

    private LocalDateTime removedAt;

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = GymCoachStatus.ACTIVE;
        }
        if (joinedAt == null) {
            joinedAt = LocalDateTime.now();
        }
    }
}
