package com.minhthien.web.coach.entity;

import com.minhthien.web.coach.enums.VideoType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "videos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String url;

    @Enumerated(EnumType.STRING)
    private VideoType type;
}
