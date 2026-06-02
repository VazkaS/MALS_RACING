package com.example.menedzeramatorskiejligisimracingowej.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "seasons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Season {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "season_year")
    private Integer year;

    @OneToMany(mappedBy = "season", cascade = CascadeType.ALL)
    private List<Race> races;

    @jakarta.persistence.Lob
    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String backgroundImage;

    private boolean archived = false;


    private boolean active = false;
}