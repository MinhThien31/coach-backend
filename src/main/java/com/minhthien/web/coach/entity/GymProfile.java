package com.minhthien.web.coach.entity;

import com.minhthien.web.coach.enums.GymProfileStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "gym_profiles",
        indexes = {
                @Index(name = "idx_gym_profiles_owner", columnList = "owner_id"),
                @Index(name = "idx_gym_profiles_status", columnList = "status")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GymProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "owner_id", nullable = false, unique = true)
    private User owner;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 255)
    private String address;

    @Column(length = 30)
    private String hotline;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String logoUrl;

    @Column(length = 500)
    private String coverUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GymProfileStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = GymProfileStatus.PENDING;
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
