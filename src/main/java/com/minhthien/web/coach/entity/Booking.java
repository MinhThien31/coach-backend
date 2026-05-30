package com.minhthien.web.coach.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.minhthien.web.coach.enums.BookingStatus;
import com.minhthien.web.coach.enums.BookingType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(
        name = "bookings",
        indexes = {
                @Index(name = "idx_bookings_trainee_start_date", columnList = "trainee_id,startDate"),
                @Index(name = "idx_bookings_coach_status", columnList = "coach_id,status"),
                @Index(name = "idx_bookings_coach_date_range", columnList = "coach_id,startDate,endDate"),
                @Index(name = "idx_bookings_coach_slot", columnList = "coach_id,dayOfWeek,startTime,status")
        }
)
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

    private LocalDate startDate;

    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;

    private String location;

    private Double price;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @Enumerated(EnumType.STRING)
    private BookingType type;   // ONLINE / OFFLINE

    private String note;        // ghi chú cho coach

    @Builder.Default
    private Boolean traineePaidUpfront = false;

    @Builder.Default
    private Boolean paymentSettled = false;

    private Long settledAmount;

    private Long adminCommissionAmount;

    private Long coachPayoutAmount;

    private LocalDateTime settledAt;

    private String cancellationReason;

    private String cancelledBy;

    private LocalDateTime cancelledAt;

    private LocalDateTime createdAt;
}
