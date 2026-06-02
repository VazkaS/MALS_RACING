package com.example.menedzeramatorskiejligisimracingowej.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LapTimeDto {

    @NotBlank(message = "Wybierz grę")
    private String game;

    @NotNull
    @Min(0)
    private Integer minutes;

    @NotNull
    @Min(0)
    @Max(59)
    private Integer seconds;

    @NotNull
    @Min(0)
    @Max(999)
    private Integer milliseconds;
}