package com.minhthien.web.coach.entity;

import com.minhthien.web.coach.enums.SubmissionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "trainee_submissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraineeSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String videoUrl;

    private String note;

    private LocalDateTime submittedAt;

    // score

    private Integer postureScore;

    private Integer techniqueScore;

    private Integer rhythmScore;

    private Integer strengthScore;

    private Double totalScore;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Enumerated(EnumType.STRING)
    private SubmissionStatus status;

    @ManyToOne
    @JoinColumn(name = "trainee_id")
    private User trainee;

    @ManyToOne
    @JoinColumn(name = "coach_id")
    private User coach;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "coach_video_id")
    private CoachVideo coachVideo;
}
