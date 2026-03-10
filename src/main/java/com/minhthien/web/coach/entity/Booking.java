package com.minhthien.web.coach.entity;

import com.minhthien.web.coach.enums.BookingStatus;
import com.minhthien.web.coach.enums.BookingType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User trainee;

    @ManyToOne
    private CoachProfile coach;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Double price;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @Enumerated(EnumType.STRING)
    private BookingType type;   // ONLINE / OFFLINE

    private String note;        // ghi chú cho coach


    private LocalDateTime createdAt;
}
