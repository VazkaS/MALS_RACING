package com.example.menedzeramatorskiejligisimracingowej.service;

import com.example.menedzeramatorskiejligisimracingowej.dto.DriverStatsDto;
import com.example.menedzeramatorskiejligisimracingowej.dto.TrackRecordDto;
import com.example.menedzeramatorskiejligisimracingowej.dto.DriverRowDto;
import com.example.menedzeramatorskiejligisimracingowej.model.RaceResult;
import com.example.menedzeramatorskiejligisimracingowej.model.Role;
import com.example.menedzeramatorskiejligisimracingowej.model.User;
import com.example.menedzeramatorskiejligisimracingowej.repository.RaceResultRepository;
import com.example.menedzeramatorskiejligisimracingowej.repository.UserRepository;
import com.example.menedzeramatorskiejligisimracingowej.repository.LapTimeRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DriverService {

    private final UserRepository userRepository;
    private final RaceResultRepository raceResultRepository;
    private final LapTimeRepository lapTimeRepository;

    public DriverService(UserRepository userRepository,
                         RaceResultRepository raceResultRepository,
                         LapTimeRepository lapTimeRepository) {
        this.userRepository = userRepository;
        this.raceResultRepository = raceResultRepository;
        this.lapTimeRepository = lapTimeRepository;
    }

    public List<User> searchDrivers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return userRepository.findByRole(Role.DRIVER);
        }
        return userRepository.findByNicknameContainingIgnoreCaseAndRole(keyword, Role.DRIVER);
    }

    public User getDriverById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Nie znaleziono kierowcy"));
    }

    public List<RaceResult> getDriverRaceHistory(User user) {
        return raceResultRepository.findByUserOrderByRaceRaceDateDesc(user);
    }

    public DriverStatsDto calculateDriverStats(User user, List<RaceResult> results) {
        DriverStatsDto stats = new DriverStatsDto();
        stats.setTotalRaces(results.size());

        // Bezpieczne pobieranie rekordów
        stats.setTrackRecords(findDriverPurpleLaps(user));

        if (results.isEmpty()) {
            // Zabezpieczenie: jeśli kierowca nie ma wyścigów, ustawiamy resztę na 0/false,
            // żeby uniknąć błędu podczas sortowania w kontrolerze.
            stats.setTotalPoints(0);
            stats.setGrandmaster(false);
            stats.setChampionships(0);
            return stats;
        }

        int dnfCount = 0;
        int podiums = 0;
        int sumPositions = 0;
        int validFinishes = 0;
        int currentSeasonPoints = 0; // ZMIENIONO: Liczymy punkty tylko z obecnego sezonu

        for (RaceResult r : results) {
            if (r.isDnf()) {
                dnfCount++;
            } else if (r.getFinalPosition() != null) {
                if (r.getFinalPosition() <= 3) {
                    podiums++;
                }
                sumPositions += r.getFinalPosition();
                validFinishes++;

                // KKLUCZOWA ZMIANA: Dodajemy punkty tylko jeśli sezon NIE jest zarchiwizowany
                if (r.getRace() != null && r.getRace().getSeason() != null && !r.getRace().getSeason().isArchived()) {
                    currentSeasonPoints += getPointsForPosition(r.getFinalPosition());
                }
            }
        }

        stats.setDnfCount(dnfCount);
        stats.setPodiums(podiums);
        stats.setAveragePosition(validFinishes > 0 ? (double) sumPositions / validFinishes : 0.0);
        stats.setPodiumRate(((double) podiums / stats.getTotalRaces()) * 100.0);

        stats.setTotalPoints(currentSeasonPoints); // Przypisujemy wyliczone punkty
        stats.setGrandmaster(checkIfGrandmaster(user));
        stats.setChampionships(countChampionships(user));

        return stats;
    }

    public List<DriverRowDto> getDriversWithStatsSorted(String keyword) {
        List<User> users = searchDrivers(keyword);

        return users.stream().map(user -> {
                    List<RaceResult> history = getDriverRaceHistory(user);
                    DriverStatsDto stats = calculateDriverStats(user, history);
                    return new DriverRowDto(user, stats);
                })
                .sorted(Comparator.comparingInt((DriverRowDto row) -> row.getStats().getTotalPoints()).reversed())
                .collect(Collectors.toList());
    }

    // ===============================================================
    // SYSTEM PUNKTACJI I GAMIFIKACJI
    // ===============================================================

    private int getPointsForPosition(int position) {
        switch(position) {
            case 1: return 25; case 2: return 18; case 3: return 15;
            case 4: return 12; case 5: return 10; case 6: return 8;
            case 7: return 6; case 8: return 4; case 9: return 2;
            case 10: return 1; default: return 0;
        }
    }

    private boolean checkIfGrandmaster(User user) {
        // 1. Pobieramy wyniki TYLKO z ZARCHIWIZOWANYCH sezonów
        List<RaceResult> archivedResults = raceResultRepository.findAll().stream()
                .filter(r -> r.getRace() != null && r.getRace().getSeason() != null && r.getRace().getSeason().isArchived())
                .filter(r -> r.getUser() != null && !r.isDnf() && r.getFinalPosition() != null)
                .toList();

        // 2. Grupujemy punkty po SEZONIE, a następnie od razu po KIEROWCY
        // (Dzięki temu sumujemy punkty z Assetto Corsa, ACC, iRacing itd. w jeden wielki wynik sezonu)
        Map<Long, Map<Long, Integer>> pointsPerArchivedSeason = archivedResults.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getRace().getSeason().getId(),
                        Collectors.groupingBy(
                                r -> r.getUser().getId(),
                                Collectors.summingInt(r -> getPointsForPosition(r.getFinalPosition()))
                        )
                ));

        // 3. Sprawdzamy, czy nasz gracz zdobył najwięcej punktów w którymkolwiek z tych sezonów
        for (Map<Long, Integer> seasonDriversPoints : pointsPerArchivedSeason.values()) {

            int myPoints = seasonDriversPoints.getOrDefault(user.getId(), 0);
            int maxPoints = seasonDriversPoints.values().stream().max(Integer::compareTo).orElse(0);

            // Jeśli zdobył punkty i jego suma to absolutny rekord tego sezonu - zostaje Grandmasterem!
            if (myPoints > 0 && myPoints == maxPoints) {
                return true;
            }
        }

        // Jeśli przejrzał wszystkie zarchiwizowane sezony i gracz nigdzie nie był pierwszy łącznie, zwraca false
        return false;
    }
    private int countChampionships(User user) {
        List<RaceResult> archivedResults = raceResultRepository.findAll().stream()
                .filter(r -> r.getUser() != null && r.getRace() != null && r.getRace().getGame() != null) // Zabezpieczenie przed nullami!
                .filter(r -> r.getRace().getSeason() != null && r.getRace().getSeason().isArchived()
                        && !r.isDnf() && r.getFinalPosition() != null)
                .toList();

        Map<Long, Map<String, Map<Long, Integer>>> standings = archivedResults.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getRace().getSeason().getId(),
                        Collectors.groupingBy(
                                r -> r.getRace().getGame(),
                                Collectors.groupingBy(
                                        r -> r.getUser().getId(),
                                        Collectors.summingInt(r -> getPointsForPosition(r.getFinalPosition()))
                                )
                        )
                ));

        int titles = 0;
        for (Map<String, Map<Long, Integer>> gamesInSeason : standings.values()) {
            for (Map<Long, Integer> driversPoints : gamesInSeason.values()) {

                long winnerId = -1;
                int maxPts = -1;

                for (Map.Entry<Long, Integer> entry : driversPoints.entrySet()) {
                    if (entry.getValue() > maxPts) {
                        maxPts = entry.getValue();
                        winnerId = entry.getKey();
                    }
                }

                if (maxPts > 0 && winnerId == user.getId()) {
                    titles++;
                }
            }
        }
        return titles;
    }

    // ===============================================================
    // WYKRYWANIE FIOLETOWYCH REKORDÓW OKRĄŻENIA (PURPLE LAPS)
    // ===============================================================
    private List<TrackRecordDto> findDriverPurpleLaps(User user) {
        List<TrackRecordDto> records = new ArrayList<>();
        var allLaps = lapTimeRepository.findAll();

        // BEZPIECZEŃSTWO: Odrzucamy lapy bez przypisanej gry lub toru
        var validLaps = allLaps.stream()
                .filter(lap -> lap.getTrack() != null && lap.getGame() != null && lap.getTimeInMillis() != null)
                .toList();

        var groupedLaps = validLaps.stream()
                .collect(Collectors.groupingBy(
                        lap -> lap.getTrack().getId(),
                        Collectors.groupingBy(lap -> lap.getGame())
                ));

        for (var trackEntry : groupedLaps.values()) {
            for (var gameLaps : trackEntry.values()) {

                var fastestLap = gameLaps.stream()
                        .min(Comparator.comparing(lap -> lap.getTimeInMillis()));

                if (fastestLap.isPresent() && fastestLap.get().getUser() != null && fastestLap.get().getUser().getId().equals(user.getId())) {
                    var lap = fastestLap.get();
                    records.add(new TrackRecordDto(
                            lap.getTrack().getName(),
                            lap.getGame(),
                            lap.getFormattedTime()
                    ));
                }
            }
        }
        return records;
    }
}