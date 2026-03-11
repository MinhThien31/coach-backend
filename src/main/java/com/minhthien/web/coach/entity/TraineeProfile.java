package com.minhthien.web.coach.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Entity
@Table(name = "trainee_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraineeProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String goal;

    private String phone;

    private Integer age;

    private Double weight;

    private Double height;

    private String avatar;

    private LocalDate joinedDate;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "coach_id")
    private CoachProfile coach;
}
