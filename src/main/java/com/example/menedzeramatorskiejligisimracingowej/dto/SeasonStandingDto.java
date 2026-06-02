package com.example.menedzeramatorskiejligisimracingowej.dto;

import com.example.menedzeramatorskiejligisimracingowej.model.User;

public class SeasonStandingDto {
    private User user;
    private int totalPoints;

    public SeasonStandingDto(User user, int totalPoints) {
        this.user = user;
        this.totalPoints = totalPoints;
    }

    public User getUser() { return user; }
    public int getTotalPoints() { return totalPoints; }
}