package com.example.menedzeramatorskiejligisimracingowej.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile; // Ważny import
import java.time.LocalDate;

@Data
public class RaceDto {

    @NotNull(message = "Wybierz sezon")
    private Long seasonId;

    @jakarta.validation.constraints.NotNull(message = "Wybierz tor")
    private Long trackId;

    @NotNull(message = "Data wyścigu jest wymagana")
    private LocalDate raceDate;

    @NotNull(message = "Liczba okrążeń jest wymagana")
    private Integer laps;

    @NotBlank(message = "Gra jest wymagana")
    private String game;

    private boolean isPrivate;


    private MultipartFile image;

    private String description;
}