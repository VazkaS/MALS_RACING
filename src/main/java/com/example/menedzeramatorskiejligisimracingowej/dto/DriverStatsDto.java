package com.example.menedzeramatorskiejligisimracingowej.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class DriverStatsDto {

    private int totalRaces;
    private int dnfCount;
    private int podiums;
    private double averagePosition;
    private double podiumRate;


    private int totalPoints;
    private boolean isGrandmaster;
    private int championships;


    private List<TrackRecordDto> trackRecords = new ArrayList<>();
}