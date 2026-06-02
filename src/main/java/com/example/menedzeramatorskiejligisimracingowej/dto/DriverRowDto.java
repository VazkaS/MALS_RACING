package com.example.menedzeramatorskiejligisimracingowej.dto;

import com.example.menedzeramatorskiejligisimracingowej.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverRowDto {
    private User driver;
    private DriverStatsDto stats;
}