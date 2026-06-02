package com.example.menedzeramatorskiejligisimracingowej.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lap_times")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LapTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "track_id", nullable = false)
    private Track track;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String game;

    @Column(nullable = false)
    private Long timeInMillis;

    @Column(nullable = false)
    private String formattedTime;
}