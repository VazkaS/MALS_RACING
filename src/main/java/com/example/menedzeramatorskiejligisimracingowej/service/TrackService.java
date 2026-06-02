package com.example.menedzeramatorskiejligisimracingowej.service;

import com.example.menedzeramatorskiejligisimracingowej.dto.LapTimeDto;
import com.example.menedzeramatorskiejligisimracingowej.model.LapTime;
import com.example.menedzeramatorskiejligisimracingowej.model.Track;
import com.example.menedzeramatorskiejligisimracingowej.model.User;
import com.example.menedzeramatorskiejligisimracingowej.repository.LapTimeRepository;
import com.example.menedzeramatorskiejligisimracingowej.repository.TrackRepository;
import com.example.menedzeramatorskiejligisimracingowej.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TrackService {

    private final TrackRepository trackRepository;
    private final LapTimeRepository lapTimeRepository;
    private final UserRepository userRepository;

    public void createTrack(com.example.menedzeramatorskiejligisimracingowej.dto.TrackDto dto) {
        Track track = new Track();
        track.setName(dto.getName());
        track.setDescription(dto.getDescription());
        trackRepository.save(track);
    }

    public TrackService(TrackRepository trackRepository, LapTimeRepository lapTimeRepository, UserRepository userRepository) {
        this.trackRepository = trackRepository;
        this.lapTimeRepository = lapTimeRepository;
        this.userRepository = userRepository;
    }

    public List<Track> getAllTracks() {
        return trackRepository.findAll();
    }

    public Track getTrackById(Long id) {
        return trackRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Nie ma takiego toru"));
    }


    public void deleteTrack(Long id) {
        trackRepository.deleteById(id);
    }

    public List<LapTime> getLeaderboard(Track track, String game) {
        return lapTimeRepository.findByTrackAndGameOrderByTimeInMillisAsc(track, game);
    }

    public boolean addOrUpdateLapTime(Long trackId, String email, LapTimeDto dto) {
        Track track = getTrackById(trackId);
        User user = userRepository.findByEmail(email);


        Long newTimeInMillis = (dto.getMinutes() * 60000L) + (dto.getSeconds() * 1000L) + dto.getMilliseconds();
        String formattedTime = String.format("%d:%02d.%03d", dto.getMinutes(), dto.getSeconds(), dto.getMilliseconds());

        LapTime existingLap = lapTimeRepository.findByTrackAndUserAndGame(track, user, dto.getGame());

        if (existingLap != null) {

            if (newTimeInMillis < existingLap.getTimeInMillis()) {
                existingLap.setTimeInMillis(newTimeInMillis);
                existingLap.setFormattedTime(formattedTime);
                lapTimeRepository.save(existingLap);
                return true;
            }
            return false;
        } else {

            LapTime lapTime = new LapTime();
            lapTime.setTrack(track);
            lapTime.setUser(user);
            lapTime.setGame(dto.getGame());
            lapTime.setTimeInMillis(newTimeInMillis);
            lapTime.setFormattedTime(formattedTime);
            lapTimeRepository.save(lapTime);
            return true;
        }
    }
}