package com.minhthien.web.coach.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "coach_schedules")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CoachSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private CoachProfile coach;

    private String dayOfWeek;

    private String startTime;

    private String endTime;
}
