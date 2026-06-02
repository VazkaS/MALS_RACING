package com.example.menedzeramatorskiejligisimracingowej.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "race_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RaceResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer finalPosition;

    private String bestLapTime;

    private boolean isDnf;
    private String penalty;

    private Integer bestLapMinutes;
    private Integer bestLapSeconds;
    private Integer bestLapMilliseconds;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "race_id", nullable = false)
    private Race race;
}