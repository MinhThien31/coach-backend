package com.minhthien.web.coach.entity;

import com.minhthien.web.coach.enums.CoachTeachingType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "coach_profiles",
        indexes = {
                @Index(name = "idx_coach_profiles_category", columnList = "category_id"),
                @Index(name = "idx_coach_profiles_rating", columnList = "rating"),
                @Index(name = "idx_coach_profiles_students", columnList = "students"),
                @Index(name = "idx_coach_profiles_price", columnList = "price")
        }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoachProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private User user;

    @ManyToOne
    private Category category;

    private Double price;

    private Double rating;

    private Integer reviewCount;

    private String avatarUrl;

    private Integer students;

    private Integer totalSessions;

    private Integer responseRate;

    private Integer experienceYears;

    @Enumerated(EnumType.STRING)
    private CoachTeachingType teachingType;

    @Column(name = "location")
    private String location;

    @Column(columnDefinition = "TEXT")
    private String bio;

}
