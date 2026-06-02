package com.example.menedzeramatorskiejligisimracingowej.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import java.util.ArrayList;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String nickname;

    @Column(unique = true)
    private Integer startingNumber;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_simulators", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "simulator")
    private List<String> preferredSimulators = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private Role role;

    @jakarta.persistence.Lob
    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String profilePicture;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @OneToMany(mappedBy = "user")
    private List<RaceResult> raceResults;

    @ManyToMany(mappedBy = "signedUpDrivers")
    private List<Race> signedUpRaces;
}