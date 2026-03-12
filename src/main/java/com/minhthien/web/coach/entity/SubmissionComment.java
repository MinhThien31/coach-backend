package com.minhthien.web.coach.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "submission_comments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "submission_id")
    private TraineeSubmission submission;

    private Integer timeSecond;

    private String comment;
}
