package com.example.menedzeramatorskiejligisimracingowej.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "penalties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Penalty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer secondsAdded;

    private String description;

    @ManyToOne
    @JoinColumn(name = "race_result_id", nullable = false)
    private RaceResult raceResult;
}