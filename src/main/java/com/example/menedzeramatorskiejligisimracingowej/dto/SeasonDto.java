package com.example.menedzeramatorskiejligisimracingowej.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SeasonDto {

    @NotBlank(message = "Nazwa sezonu jest wymagana")
    private String name;

    @NotNull(message = "Rok jest wymagany")
    private Integer year;

    private org.springframework.web.multipart.MultipartFile image;
}