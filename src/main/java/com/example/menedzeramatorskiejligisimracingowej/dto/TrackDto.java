package com.example.menedzeramatorskiejligisimracingowej.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TrackDto {
    @NotBlank(message = "Nazwa toru jest wymagana")
    private String name;
    private String description;
}