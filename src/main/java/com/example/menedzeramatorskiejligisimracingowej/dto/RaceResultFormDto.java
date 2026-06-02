package com.example.menedzeramatorskiejligisimracingowej.dto;

import lombok.Data;
import java.util.List;

@Data
public class RaceResultFormDto {
    private List<RaceResultEntryDto> entries;
}