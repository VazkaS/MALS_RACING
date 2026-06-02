package com.example.menedzeramatorskiejligisimracingowej.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "races")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Race {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "track_id", nullable = false)
    private Track track;

    @Column(nullable = false)
    private LocalDate raceDate;

    private Integer laps;

    @Column(nullable = false)
    private String game;


    private boolean archived = false;

    private boolean isPrivate;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String backgroundImage;


    @Column(length = 2000)
    private String description;

    @ManyToMany
    @JoinTable(
            name = "race_signups",
            joinColumns = @JoinColumn(name = "race_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> signedUpDrivers = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @OneToMany(mappedBy = "race", cascade = CascadeType.ALL)
    private List<RaceResult> results;
}