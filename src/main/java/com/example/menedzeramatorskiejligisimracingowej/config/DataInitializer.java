package com.example.menedzeramatorskiejligisimracingowej.config;

import com.example.menedzeramatorskiejligisimracingowej.dto.LapTimeDto;
import com.example.menedzeramatorskiejligisimracingowej.service.TrackService;
import org.springframework.core.io.ClassPathResource;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import com.example.menedzeramatorskiejligisimracingowej.model.Race;
import com.example.menedzeramatorskiejligisimracingowej.model.RaceResult;
import com.example.menedzeramatorskiejligisimracingowej.model.Role;
import com.example.menedzeramatorskiejligisimracingowej.model.Season;
import com.example.menedzeramatorskiejligisimracingowej.model.Track;
import com.example.menedzeramatorskiejligisimracingowej.model.User;
import com.example.menedzeramatorskiejligisimracingowej.repository.RaceRepository;
import com.example.menedzeramatorskiejligisimracingowej.repository.RaceResultRepository;
import com.example.menedzeramatorskiejligisimracingowej.repository.SeasonRepository;
import com.example.menedzeramatorskiejligisimracingowej.repository.TrackRepository;
import com.example.menedzeramatorskiejligisimracingowej.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SeasonRepository seasonRepository;
    private final RaceRepository raceRepository;
    private final TrackRepository trackRepository;
    private final RaceResultRepository raceResultRepository;
    private final TrackService trackService;

    public DataInitializer(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           SeasonRepository seasonRepository,
                           RaceRepository raceRepository,
                           TrackRepository trackRepository,
                           RaceResultRepository raceResultRepository,
                           TrackService trackService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.seasonRepository = seasonRepository;
        this.raceRepository = raceRepository;
        this.trackRepository = trackRepository;
        this.raceResultRepository = raceResultRepository;
        this.trackService = trackService;
    }

    @Override
    public void run(String... args) throws Exception {

        // ==========================================
        // 1. GENEROWANIE UŻYTKOWNIKÓW (1 Admin + 96 Kierowców)
        // ==========================================
        if (!userRepository.existsByEmail("admin@mals.pl")) {
            User admin = new User();
            admin.setEmail("admin@mals.pl");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setNickname("Dyrektor Ligi");
            admin.setStartingNumber(null);
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
        }

        if (userRepository.count() <= 1) {
            String[] simNames = {"Assetto Corsa", "Assetto Corsa Evo", "Assetto Corsa Competizione", "iRacing"};
            String[] adjs = {"Fast", "Turbo", "Apex", "Grip", "Slip", "Red", "Blue", "Mad", "Ice", "Hot", "Iron", "Steel", "Dark", "Light", "Pro", "Star", "Mega", "Ultra", "Super", "Hyper", "Neon", "Cyber", "Aero", "Mech"};
            String[] nouns = {"Racer", "Driver", "Pilot", "Gonzo", "Janusz", "Braker", "Master", "Rider", "Boss", "Pro", "King", "Hunter", "Chaser", "Crusher", "Sniper", "Striker", "Runner", "Walker", "Dasher", "Flyer", "Glider", "Surfer", "Drifter", "Slider"};

            int count = 1;
            for (String sim : simNames) {
                for (int i = 0; i < 24; i++) {
                    User u = new User();
                    u.setEmail("driver" + count + "@mals.pl");
                    u.setPassword(passwordEncoder.encode("kamil"));
                    u.setNickname(adjs[count % adjs.length] + nouns[count % nouns.length] + count);
                    u.setStartingNumber(count);
                    u.setRole(Role.DRIVER);
                    u.setPreferredSimulators(List.of(sim));
                    userRepository.save(u);
                    count++;
                }
            }
        }

        List<User> allDrivers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.DRIVER)
                .toList();

        // ==========================================
        // 2. GENEROWANIE TORÓW (24 tory F1)
        // ==========================================
        if (trackRepository.count() == 0) {
            String[][] f1Tracks = {
                    {"Bahrain International Circuit", "Pustynny tor rozpoczynający sezon pod sztucznym oświetleniem."},
                    {"Jeddah Corniche Circuit", "Najszybszy tor uliczny w kalendarzu, położony nad Morzem Czerwonym."},
                    {"Albert Park Circuit", "Szybki tor w centrum Melbourne, w Australii."},
                    {"Suzuka International Racing Course", "Legendarny tor w kształcie ósemki w Japonii."},
                    {"Shanghai International Circuit", "Techniczny tor w Chinach ze słynnym, ślimakowym zakrętem T1."},
                    {"Miami International Autodrome", "Kolorowy tor uliczny wokół stadionu Hard Rock w Miami."},
                    {"Imola Circuit", "Klasyczny, wymagający tor we Włoszech, pełen historii."},
                    {"Circuit de Monaco", "Najbardziej prestiżowy, ciasny tor uliczny na świecie."},
                    {"Circuit Gilles Villeneuve", "Szybki tor w Kanadzie ze słynną Ścianą Mistrzów."},
                    {"Circuit de Barcelona-Catalunya", "Wszechstronny tor w Hiszpanii, doskonały test aerodynamiki."},
                    {"Red Bull Ring", "Krótki, szybki tor w austriackich Alpach z dużymi różnicami wzniesień."},
                    {"Silverstone Circuit", "Kolebka Formuły 1, jeden z najszybszych torów w kalendarzu."},
                    {"Hungaroring", "Kręty i techniczny tor na Węgrzech, nazywany 'Monaco bez ścian'."},
                    {"Circuit de Spa-Francorchamps", "Najdłuższy tor w kalendarzu z legendarnym zakrętem Eau Rouge."},
                    {"Circuit Zandvoort", "Tor w Holandii ze stromo pochylonymi zakrętami na wydmach."},
                    {"Monza Circuit", "Świątynia Prędkości we Włoszech. Najszybszy tor w kalendarzu."},
                    {"Baku City Circuit", "Bardzo szybki tor uliczny w Azerbejdżanie z ciasną sekcją zamkową."},
                    {"Marina Bay Street Circuit", "Wymagający fizycznie nocny wyścig uliczny w Singapurze."},
                    {"Circuit of the Americas (COTA)", "Nowoczesny tor w Teksasie, czerpiący inspiracje z najlepszych sekcji innych torów."},
                    {"Autódromo Hermanos Rodríguez", "Tor w Meksyku położony na dużej wysokości, z rzadkim powietrzem."},
                    {"Interlagos Circuit", "Kultowy, pełen emocji tor w Brazylii z jazdą przeciwnie do wskazówek zegara."},
                    {"Las Vegas Strip Circuit", "Szybki, nocny wyścig uliczny w sercu stolicy hazardu."},
                    {"Lusail International Circuit", "Szybki tor w Katarze, bardzo obciążający opony."},
                    {"Yas Marina Circuit", "Nowoczesny tor w Abu Zabi, tradycyjnie kończący sezon."}
            };
            for (String[] data : f1Tracks) {
                Track track = new Track(); track.setName(data[0]); track.setDescription(data[1]); trackRepository.save(track);
            }
        }

        List<Track> allTracks = trackRepository.findAll();

        // ==========================================
        // 3. GENEROWANIE SEZONÓW
        // ==========================================
        Season winter25 = null, spring26 = null, summer26 = null, autumn26 = null, winter26 = null;

        if (seasonRepository.findAll().isEmpty()) {
            winter25 = new Season(); winter25.setName("Zima 2025/2026"); winter25.setYear(2025); winter25.setArchived(true); winter25.setActive(false); winter25.setBackgroundImage(encodeImageToBase64("static/photos/zima25.webp")); winter25 = seasonRepository.save(winter25);
            spring26 = new Season(); spring26.setName("Wiosna 2026"); spring26.setYear(2026); spring26.setArchived(false); spring26.setActive(true); spring26.setBackgroundImage(encodeImageToBase64("static/photos/wiosna.jpg")); spring26 = seasonRepository.save(spring26);
            summer26 = new Season(); summer26.setName("Lato 2026"); summer26.setYear(2026); summer26.setArchived(false); summer26.setActive(false); summer26.setBackgroundImage(encodeImageToBase64("static/photos/summer.jpg")); summer26 = seasonRepository.save(summer26);
            autumn26 = new Season(); autumn26.setName("Jesień 2026"); autumn26.setYear(2026); autumn26.setArchived(false); autumn26.setActive(false); autumn26.setBackgroundImage(encodeImageToBase64("static/photos/autumn.jpg")); autumn26 = seasonRepository.save(autumn26);
            winter26 = new Season(); winter26.setName("Zima 2026/2027"); winter26.setYear(2026); winter26.setArchived(false); winter26.setActive(false); winter26.setBackgroundImage(encodeImageToBase64("static/photos/zima.webp")); winter26 = seasonRepository.save(winter26);
        } else {
            List<Season> seasons = seasonRepository.findAll();
            winter25 = seasons.stream().filter(s -> s.getName().equals("Zima 2025/2026")).findFirst().orElse(seasons.get(0));
            spring26 = seasons.stream().filter(s -> s.getName().equals("Wiosna 2026")).findFirst().orElse(seasons.get(0));
        }

        // ==========================================
        // 4. POTĘŻNY GENERATOR KALENDARZA (192 Wyścigi)
        // ==========================================
        if (raceRepository.count() == 0 && allDrivers.size() >= 96 && allTracks.size() == 24) {
            String[] simNames = {"Assetto Corsa", "Assetto Corsa Evo", "Assetto Corsa Competizione", "iRacing"};
            LocalDate today = LocalDate.now();

            LocalDate winterStart = LocalDate.of(2025, 11, 1);
            for (String sim : simNames) {
                List<User> simDrivers = getDriversForSim(allDrivers, sim);
                for (int i = 0; i < allTracks.size(); i++) {
                    Race r = new Race();
                    r.setTrack(allTracks.get(i));
                    r.setRaceDate(winterStart.plusDays(i * 5));
                    r.setLaps(25);
                    r.setGame(sim);
                    r.setSeason(winter25);
                    r.setArchived(true);
                    Race savedRace = raceRepository.save(r);
                    generateResultsForRace(savedRace, simDrivers);
                }
            }

            LocalDate springStart = LocalDate.of(2026, 3, 1);
            for (String sim : simNames) {
                List<User> simDrivers = getDriversForSim(allDrivers, sim);
                for (int i = 0; i < allTracks.size(); i++) {
                    LocalDate raceDate = springStart.plusDays(i * 5);

                    Race r = new Race();
                    r.setTrack(allTracks.get(i));
                    r.setRaceDate(raceDate);
                    r.setLaps(25);
                    r.setGame(sim);
                    r.setSeason(spring26);

                    boolean isCompleted = raceDate.isBefore(today) || raceDate.isEqual(today);
                    r.setArchived(isCompleted);
                    Race savedRace = raceRepository.save(r);

                    if (isCompleted) {
                        generateResultsForRace(savedRace, simDrivers);
                    } else {
                        savedRace.getSignedUpDrivers().addAll(simDrivers);
                        raceRepository.save(savedRace);
                    }
                }
            }
        }
    }

    // ==========================================
    // METODY POMOCNICZE
    // ==========================================

    private List<User> getDriversForSim(List<User> drivers, String game) {
        return drivers.stream()
                .filter(u -> u.getPreferredSimulators().contains(game))
                .toList();
    }

    private void generateResultsForRace(Race race, List<User> drivers) {
        List<User> shuffledDrivers = new ArrayList<>(drivers);
        Collections.shuffle(shuffledDrivers);

        int position = 1;
        for (int i = 0; i < shuffledDrivers.size(); i++) {
            User driver = shuffledDrivers.get(i);

            boolean isDnf = (i >= shuffledDrivers.size() - 3);

            Integer min = isDnf ? null : 1 + (int)(Math.random() * 2);
            Integer sec = isDnf ? null : 10 + (int)(Math.random() * 40);
            Integer ms = isDnf ? null : (int)(Math.random() * 900);
            Integer finalPosition = isDnf ? null : position++;

            raceResultRepository.save(createResult(race, driver, finalPosition, isDnf, min, sec, ms));

            if (!isDnf) {
                saveLapToLeaderboard(race.getTrack().getId(), driver.getEmail(), race.getGame(), min, sec, ms);
            }
        }
    }

    private RaceResult createResult(Race race, User user, Integer position, boolean isDnf, Integer min, Integer sec, Integer ms) {
        RaceResult result = new RaceResult();
        result.setRace(race);
        result.setUser(user);
        result.setFinalPosition(position);
        result.setDnf(isDnf);
        result.setBestLapMinutes(min);
        result.setBestLapSeconds(sec);
        result.setBestLapMilliseconds(ms);
        return result;
    }

    private void saveLapToLeaderboard(Long trackId, String email, String game, Integer min, Integer sec, Integer ms) {
        if (min == null || sec == null || ms == null) return;
        LapTimeDto dto = new LapTimeDto();
        dto.setGame(game);
        dto.setMinutes(min);
        dto.setSeconds(sec);
        dto.setMilliseconds(ms);
        trackService.addOrUpdateLapTime(trackId, email, dto);
    }

    private String encodeImageToBase64(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            if (resource.exists()) {
                byte[] imageBytes = resource.getInputStream().readAllBytes();
                return Base64.getEncoder().encodeToString(imageBytes);
            }
        } catch (Exception e) {
        }
        return null;
    }
}