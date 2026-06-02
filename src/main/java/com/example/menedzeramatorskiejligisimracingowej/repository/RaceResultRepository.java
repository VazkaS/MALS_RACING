package com.example.menedzeramatorskiejligisimracingowej.repository;

import com.example.menedzeramatorskiejligisimracingowej.model.Race;
import com.example.menedzeramatorskiejligisimracingowej.model.RaceResult;
import com.example.menedzeramatorskiejligisimracingowej.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RaceResultRepository extends JpaRepository<RaceResult, Long> {

    List<RaceResult> findByUserOrderByRaceRaceDateDesc(User user);

    List<RaceResult> findByRaceSeasonAndRaceGame(com.example.menedzeramatorskiejligisimracingowej.model.Season season, String game);

    List<RaceResult> findByRace(Race race);
}