package com.minhthien.web.coach.entity;

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

    private String videoUrl;

    private String format;

    private Double size;

    private String resolution;

    private LocalDate uploadDate;

    private String tags;

    @ManyToOne
    private Category category;

    @ManyToOne
    private User coach;
}
