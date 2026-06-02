package com.example.menedzeramatorskiejligisimracingowej.service;

import com.example.menedzeramatorskiejligisimracingowej.dto.LapTimeDto;
import com.example.menedzeramatorskiejligisimracingowej.dto.RaceDto;
import com.example.menedzeramatorskiejligisimracingowej.dto.RaceResultEntryDto;
import com.example.menedzeramatorskiejligisimracingowej.dto.RaceResultFormDto;
import com.example.menedzeramatorskiejligisimracingowej.model.Race;
import com.example.menedzeramatorskiejligisimracingowej.model.RaceResult;
import com.example.menedzeramatorskiejligisimracingowej.model.Season;
import com.example.menedzeramatorskiejligisimracingowej.model.Track;
import com.example.menedzeramatorskiejligisimracingowej.model.User;
import com.example.menedzeramatorskiejligisimracingowej.repository.RaceRepository;
import com.example.menedzeramatorskiejligisimracingowej.repository.RaceResultRepository;
import com.example.menedzeramatorskiejligisimracingowej.repository.SeasonRepository;
import com.example.menedzeramatorskiejligisimracingowej.repository.TrackRepository;
import com.example.menedzeramatorskiejligisimracingowej.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Service
public class RaceService {

    private final RaceRepository raceRepository;
    private final SeasonRepository seasonRepository;
    private final UserRepository userRepository;
    private final TrackRepository trackRepository;
    private final RaceResultRepository raceResultRepository;
    private final TrackService trackService;

    public RaceService(RaceRepository raceRepository,
                       SeasonRepository seasonRepository,
                       UserRepository userRepository,
                       TrackRepository trackRepository,
                       RaceResultRepository raceResultRepository,
                       TrackService trackService) {
        this.raceRepository = raceRepository;
        this.seasonRepository = seasonRepository;
        this.userRepository = userRepository;
        this.trackRepository = trackRepository;
        this.raceResultRepository = raceResultRepository;
        this.trackService = trackService;
    }

    public void createRace(RaceDto raceDto) {
        Season season = seasonRepository.findById(raceDto.getSeasonId())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono sezonu"));

        Track track = trackRepository.findById(raceDto.getTrackId())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono toru"));

        Race race = new Race();
        race.setTrack(track);
        race.setRaceDate(raceDto.getRaceDate());
        race.setLaps(raceDto.getLaps());
        race.setGame(raceDto.getGame());
        race.setPrivate(raceDto.isPrivate());
        race.setSeason(season);
        race.setDescription(raceDto.getDescription());

        if (raceDto.getImage() != null && !raceDto.getImage().isEmpty()) {
            try {
                byte[] imageBytes = raceDto.getImage().getBytes();
                String base64Encoded = Base64.getEncoder().encodeToString(imageBytes);
                race.setBackgroundImage(base64Encoded);
            } catch (IOException e) {
                System.err.println("Błąd podczas przetwarzania pliku graficznego: " + e.getMessage());
            }
        }
        raceRepository.save(race);
    }

    public void updateRace(Long id, RaceDto raceDto) {
        Race race = getRaceById(id);

        Season season = seasonRepository.findById(raceDto.getSeasonId())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono sezonu"));
        Track track = trackRepository.findById(raceDto.getTrackId())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono toru"));

        race.setTrack(track);
        race.setRaceDate(raceDto.getRaceDate());
        race.setLaps(raceDto.getLaps());
        race.setGame(raceDto.getGame());
        race.setPrivate(raceDto.isPrivate());
        race.setSeason(season);
        race.setDescription(raceDto.getDescription());

        if (raceDto.getImage() != null && !raceDto.getImage().isEmpty()) {
            try {
                byte[] imageBytes = raceDto.getImage().getBytes();
                String base64Encoded = Base64.getEncoder().encodeToString(imageBytes);
                race.setBackgroundImage(base64Encoded);
            } catch (IOException e) {
                System.err.println("Błąd podczas przetwarzania pliku graficznego: " + e.getMessage());
            }
        }
        raceRepository.save(race);
    }

    public List<Race> getUpcomingRaces() {
        return raceRepository.findAllByArchivedFalseOrderByRaceDateAsc();
    }

    public List<Race> getPastRaces() {
        return raceRepository.findAllByArchivedTrueOrderByRaceDateDesc();
    }

    public List<Race> getDriverUpcomingRaces(User driver) {
        return raceRepository.findBySignedUpDriversContainingAndArchivedFalseOrderByRaceDateAsc(driver);
    }

    public Race getRaceById(Long id) {
        return raceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono wyścigu o ID: " + id));
    }

    public void signUpDriverToRace(Long raceId, String userEmail) {
        Race race = getRaceById(raceId);
        if (race.isArchived()) {
            throw new IllegalStateException("Zapisy na ten wyścig są już zamknięte.");
        }
        User user = userRepository.findByEmail(userEmail);
        if (!race.getSignedUpDrivers().contains(user)) {
            race.getSignedUpDrivers().add(user);
            raceRepository.save(race);
        }
    }

    public List<RaceResult> getRaceResults(Race race) {
        List<RaceResult> results = raceResultRepository.findByRace(race);
        results.sort((r1, r2) -> {
            if (r1.isDnf() && !r2.isDnf()) return 1;
            if (!r1.isDnf() && r2.isDnf()) return -1;
            if (r1.isDnf() && r2.isDnf()) return 0;
            return Integer.compare(r1.getFinalPosition(), r2.getFinalPosition());
        });
        return results;
    }

    public void saveRaceResultsAndArchive(Long raceId, RaceResultFormDto formDto) {
        Race race = getRaceById(raceId);

        for (RaceResultEntryDto entry : formDto.getEntries()) {
            User user = userRepository.findById(entry.getUserId()).orElseThrow();

            RaceResult result = new RaceResult();
            result.setRace(race);
            result.setUser(user);
            result.setFinalPosition(entry.getPosition());
            result.setDnf(entry.isDnf());
            result.setBestLapMinutes(entry.getMin());
            result.setBestLapSeconds(entry.getSec());
            result.setBestLapMilliseconds(entry.getMs());
            raceResultRepository.save(result);

            if (entry.getMin() != null && entry.getSec() != null && entry.getMs() != null) {
                LapTimeDto lap = new LapTimeDto();
                lap.setGame(race.getGame());
                lap.setMinutes(entry.getMin());
                lap.setSeconds(entry.getSec());
                lap.setMilliseconds(entry.getMs());
                trackService.addOrUpdateLapTime(race.getTrack().getId(), user.getEmail(), lap);
            }
        }
        race.setArchived(true);
        raceRepository.save(race);
    }
}