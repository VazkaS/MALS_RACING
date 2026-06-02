package com.example.menedzeramatorskiejligisimracingowej.repository;

import com.example.menedzeramatorskiejligisimracingowej.model.LapTime;
import com.example.menedzeramatorskiejligisimracingowej.model.Track;
import com.example.menedzeramatorskiejligisimracingowej.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LapTimeRepository extends JpaRepository<LapTime, Long> {

    List<LapTime> findByTrackAndGameOrderByTimeInMillisAsc(Track track, String game);


    LapTime findByTrackAndUserAndGame(Track track, User user, String game);
}