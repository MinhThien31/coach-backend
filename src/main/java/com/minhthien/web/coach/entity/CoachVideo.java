package com.minhthien.web.coach.entity;

import com.minhthien.web.coach.enums.VideoType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "coach_videos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoachVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String videoUrl;

    private String thumbnailUrl;

    private String format;

    private Double size;

    private String resolution;

    private Long duration;

    private String difficulty;

    private String visibility;

    @Builder.Default
    private Long likes = 0L;

    @Builder.Default
    private Boolean isPremium = false;

    private LocalDate uploadDate;

    @Enumerated(EnumType.STRING)
    private VideoType videoType;

    @Column(name = "view_count")
    @Builder.Default
    private Long viewCount = 0L;

    private String tags;

    @ManyToOne
    private Category category;

    @ManyToOne
    private User coach;
}
