package com.example.menedzeramatorskiejligisimracingowej.dto;

import com.example.menedzeramatorskiejligisimracingowej.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

@Data
public class UserRegistrationDto {

    @NotBlank(message = "Email jest wymagany")
    @Email(message = "Podaj poprawny format adresu email")
    private String email;

    @NotBlank(message = "Hasło jest wymagane")
    private String password;

    @NotBlank(message = "Pseudonim jest wymagany")
    private String nickname;

    @Min(value = 1, message = "Numer startowy musi wynosić minimum 1")
    @Max(value = 999, message = "Numer startowy może wynosić maksymalnie 999")
    private Integer startingNumber;

    @NotNull(message = "Wybór roli jest wymagany")
    private Role role;

    private String adminCode;
}