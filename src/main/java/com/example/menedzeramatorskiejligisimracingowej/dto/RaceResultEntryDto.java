package com.example.menedzeramatorskiejligisimracingowej.dto;

import lombok.Data;

@Data
public class RaceResultEntryDto {
    private Long userId;
    private String nickname;
    private Integer position;
    private Integer min;
    private Integer sec;
    private Integer ms;
    private boolean dnf;
    private String penalty;
}