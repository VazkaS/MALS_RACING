package com.example.menedzeramatorskiejligisimracingowej.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
public class UserProfileDto {

    @NotNull(message = "Numer startowy jest wymagany")
    @Min(value = 1, message = "Numer startowy musi wynosić minimum 1")
    @Max(value = 999, message = "Numer startowy może wynosić maksymalnie 999")
    private Integer startingNumber;

    @NotEmpty(message = "Wybierz przynajmniej jeden symulator")
    private java.util.List<String> preferredSimulators;

    private String nickname;
    private String email;
    private String newPassword; // Opcjonalne nowe hasło
    private MultipartFile profilePicture; // Plik ze zdjęciem
}