package com.example.menedzeramatorskiejligisimracingowej.service;

import com.example.menedzeramatorskiejligisimracingowej.dto.SeasonDto;
import com.example.menedzeramatorskiejligisimracingowej.dto.SeasonStandingDto;
import com.example.menedzeramatorskiejligisimracingowej.model.RaceResult;
import com.example.menedzeramatorskiejligisimracingowej.model.Season;
import com.example.menedzeramatorskiejligisimracingowej.model.User;
import com.example.menedzeramatorskiejligisimracingowej.repository.RaceResultRepository;
import com.example.menedzeramatorskiejligisimracingowej.repository.SeasonRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SeasonService {

    private final SeasonRepository seasonRepository;
    private final RaceResultRepository raceResultRepository;

    public SeasonService(SeasonRepository seasonRepository, RaceResultRepository raceResultRepository) {
        this.seasonRepository = seasonRepository;
        this.raceResultRepository = raceResultRepository;
    }

    public void createSeason(SeasonDto seasonDto) {
        Season season = new Season();
        season.setName(seasonDto.getName());
        season.setYear(seasonDto.getYear());

        season.setArchived(false);
        season.setActive(false);

        if (seasonDto.getImage() != null && !seasonDto.getImage().isEmpty()) {
            try {
                byte[] imageBytes = seasonDto.getImage().getBytes();
                String base64Encoded = java.util.Base64.getEncoder().encodeToString(imageBytes);
                season.setBackgroundImage(base64Encoded);
            } catch (java.io.IOException e) {
                System.err.println("Błąd grafiki: " + e.getMessage());
            }
        }
        seasonRepository.save(season);
    }

    // ==============================================================
    // ROZDZIELENIE SEZONÓW NA 3 LISTY: TRWAJĄCE, OCZEKUJĄCE, ZAKOŃCZONE
    // ==============================================================

    public List<Season> getActiveSeasons() {
        return seasonRepository.findAllByArchivedFalseAndActiveTrue();
    }

    public List<Season> getUpcomingSeasons() {
        return seasonRepository.findAllByArchivedFalseAndActiveFalse();
    }

    public List<Season> getArchivedSeasons() {
        return seasonRepository.findAllByArchivedTrue();
    }

    public Season getSeasonById(Long id) {
        return seasonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono sezonu: " + id));
    }

    public void archiveSeason(Long id) {
        Season season = getSeasonById(id);
        season.setArchived(true);
        season.setActive(false); // Traci status aktywnego
        seasonRepository.save(season);
    }


    @Scheduled(cron = "0 0 0 * * *")
    public void autoArchiveSeasonsOnSeasonChange() {
        LocalDate dzisiaj = LocalDate.now();

        boolean czyZmianaPoryRoku =
                (dzisiaj.getMonthValue() == 3 && dzisiaj.getDayOfMonth() == 21) || // Wiosna
                        (dzisiaj.getMonthValue() == 6 && dzisiaj.getDayOfMonth() == 21) || // Lato
                        (dzisiaj.getMonthValue() == 9 && dzisiaj.getDayOfMonth() == 23) || // Jesień
                        (dzisiaj.getMonthValue() == 12 && dzisiaj.getDayOfMonth() == 22);  // Zima

        if (czyZmianaPoryRoku) {
            List<Season> aktywneSezony = getActiveSeasons();
            for (Season sezon : aktywneSezony) {
                sezon.setArchived(true);
                sezon.setActive(false);
                seasonRepository.save(sezon);
                System.out.println("AUTOMAT: Zakończono sezon: " + sezon.getName());
            }

            List<Season> nadchodzace = getUpcomingSeasons();
            if (!nadchodzace.isEmpty()) {
                Season nastepnySezon = nadchodzace.get(0);
                nastepnySezon.setActive(true);
                seasonRepository.save(nastepnySezon);
                System.out.println("AUTOMAT: Rozpoczęto nowy sezon ligowy: " + nastepnySezon.getName());
            }
        }
    }

    // ==============================================================
    // OBLICZANIE PUNKTÓW
    // ==============================================================

    public List<SeasonStandingDto> getSeasonStandings(Season season, String game) {
        List<RaceResult> results = raceResultRepository.findByRaceSeasonAndRaceGame(season, game);
        Map<User, Integer> pointsMap = new HashMap<>();

        for (RaceResult r : results) {
            pointsMap.putIfAbsent(r.getUser(), 0);
            if (!r.isDnf() && r.getFinalPosition() != null) {
                pointsMap.put(r.getUser(), pointsMap.get(r.getUser()) + calculatePoints(r.getFinalPosition()));
            }
        }
        List<SeasonStandingDto> standings = new ArrayList<>();
        for (Map.Entry<User, Integer> entry : pointsMap.entrySet()) {
            standings.add(new SeasonStandingDto(entry.getKey(), entry.getValue()));
        }
        standings.sort((s1, s2) -> Integer.compare(s2.getTotalPoints(), s1.getTotalPoints()));
        return standings;
    }

    private int calculatePoints(int position) {
        int[] points = {0, 25, 18, 15, 12, 10, 8, 6, 4, 2, 1};
        return (position >= 1 && position <= 10) ? points[position] : 0;
    }
    public void activateSeason(Long id) {
        Season season = getSeasonById(id);
        season.setActive(true);
        season.setArchived(false); // Dla pewności, że nie jest w archiwum
        seasonRepository.save(season);
    }
}