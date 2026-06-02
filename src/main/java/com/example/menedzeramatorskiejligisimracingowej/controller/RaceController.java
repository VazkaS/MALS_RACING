package com.example.menedzeramatorskiejligisimracingowej.controller;

import com.example.menedzeramatorskiejligisimracingowej.dto.RaceDto;
import com.example.menedzeramatorskiejligisimracingowej.dto.RaceResultEntryDto;
import com.example.menedzeramatorskiejligisimracingowej.dto.RaceResultFormDto;
import com.example.menedzeramatorskiejligisimracingowej.model.Race;
import com.example.menedzeramatorskiejligisimracingowej.model.User;
import com.example.menedzeramatorskiejligisimracingowej.service.RaceService;
import com.example.menedzeramatorskiejligisimracingowej.service.SeasonService;
import com.example.menedzeramatorskiejligisimracingowej.service.TrackService;
import com.example.menedzeramatorskiejligisimracingowej.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class RaceController {

    private final RaceService raceService;
    private final SeasonService seasonService;
    private final UserService userService;
    private final TrackService trackService;

    public RaceController(RaceService raceService,
                          SeasonService seasonService,
                          UserService userService,
                          TrackService trackService) {
        this.raceService = raceService;
        this.seasonService = seasonService;
        this.userService = userService;
        this.trackService = trackService;
    }

    @PostMapping("/races")
    public String addRace(@Valid @ModelAttribute("newRace") RaceDto raceDto, BindingResult result, Model model, HttpServletRequest request) {
        if (!request.isUserInRole("ROLE_ADMIN")) return "redirect:/races?error=BrakUprawnien";
        if (result.hasErrors()) {
            model.addAttribute("isAdmin", true);
            model.addAttribute("upcomingRaces", raceService.getUpcomingRaces());
            model.addAttribute("pastRaces", raceService.getPastRaces());
            model.addAttribute("seasons", seasonService.getActiveSeasons());
            model.addAttribute("tracks", trackService.getAllTracks());
            return "races";
        }
        raceService.createRace(raceDto);
        return "redirect:/races?success";
    }

    @GetMapping("/races")
    public String showRaces(Model model, HttpServletRequest request) {
        boolean isAdmin = request.isUserInRole("ROLE_ADMIN");
        model.addAttribute("isAdmin", isAdmin);

        List<Race> upcoming = raceService.getUpcomingRaces();
        List<Race> past = raceService.getPastRaces();

        if (!isAdmin) {
            upcoming = upcoming.stream().filter(r -> !r.isPrivate()).toList();
            past = past.stream().filter(r -> !r.isPrivate()).toList();
        }

        model.addAttribute("upcomingRaces", upcoming);
        model.addAttribute("pastRaces", past);

        if (isAdmin) {
            model.addAttribute("newRace", new RaceDto());
            model.addAttribute("seasons", seasonService.getActiveSeasons());
            model.addAttribute("tracks", trackService.getAllTracks());
        }
        return "races";
    }

    @GetMapping("/races/{id}")
    public String showRaceDetails(@PathVariable Long id, Model model, Principal principal, HttpServletRequest request) {
        Race race = raceService.getRaceById(id);
        boolean isAdmin = request.isUserInRole("ROLE_ADMIN");

        if (race.isPrivate() && !isAdmin) {
            return "redirect:/races?error=WyscigPrywatny";
        }

        boolean isAlreadySignedUp = false;
        if (!isAdmin && principal != null) {
            User currentUser = userService.findByEmail(principal.getName());
            isAlreadySignedUp = race.getSignedUpDrivers().contains(currentUser);
        }

        model.addAttribute("race", race);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isAlreadySignedUp", isAlreadySignedUp);
        model.addAttribute("isArchived", race.isArchived());
        model.addAttribute("raceResults", raceService.getRaceResults(race));

        return "race-details";
    }

    @PostMapping("/races/{id}/signup")
    public String signUpForRace(@PathVariable Long id, Principal principal, HttpServletRequest request) {
        if (principal == null) return "redirect:/login";
        if (request.isUserInRole("ROLE_ADMIN")) return "redirect:/races/" + id + "?error=AdminNieMoze";

        Race race = raceService.getRaceById(id);
        User currentUser = userService.findByEmail(principal.getName());

        if (race != null && currentUser != null) {

            var userSims = currentUser.getPreferredSimulators();
            String raceGame = race.getGame();

            if (userSims == null || raceGame == null || !userSims.contains(raceGame)) {
                return "redirect:/races/" + id + "?error=WrongSimulator";
            }
        }

        raceService.signUpDriverToRace(id, principal.getName());
        return "redirect:/races/" + id + "?success";
    }

    @GetMapping("/races/{id}/results")
    public String showResultsForm(@PathVariable Long id, Model model, HttpServletRequest request) {
        if (!request.isUserInRole("ROLE_ADMIN")) return "redirect:/races/" + id + "?error=BrakUprawnien";

        Race race = raceService.getRaceById(id);
        if (race.isArchived()) return "redirect:/races/" + id;

        RaceResultFormDto formDto = new RaceResultFormDto();
        List<RaceResultEntryDto> entries = new ArrayList<>();

        for (User driver : race.getSignedUpDrivers()) {
            RaceResultEntryDto entry = new RaceResultEntryDto();
            entry.setUserId(driver.getId());
            entry.setNickname(driver.getNickname());
            entries.add(entry);
        }
        formDto.setEntries(entries);

        model.addAttribute("race", race);
        model.addAttribute("formDto", formDto);
        return "race-results-form";
    }

    @PostMapping("/races/{id}/results")
    public String submitResultsForm(@PathVariable Long id, @ModelAttribute("formDto") RaceResultFormDto formDto, HttpServletRequest request) {
        if (!request.isUserInRole("ROLE_ADMIN")) return "redirect:/races/" + id + "?error=BrakUprawnien";

        raceService.saveRaceResultsAndArchive(id, formDto);
        return "redirect:/races/" + id + "?success=WyscigZakonczony";
    }

    @GetMapping("/races/{id}/edit")
    public String showEditRaceForm(@PathVariable Long id, Model model, HttpServletRequest request) {
        if (!request.isUserInRole("ROLE_ADMIN")) return "redirect:/races/" + id + "?error=BrakUprawnien";

        Race race = raceService.getRaceById(id);
        if (race.isArchived()) return "redirect:/races/" + id + "?error=WyscigZakonczony";

        RaceDto raceDto = new RaceDto();
        raceDto.setTrackId(race.getTrack().getId());
        raceDto.setSeasonId(race.getSeason().getId());
        raceDto.setRaceDate(race.getRaceDate());
        raceDto.setLaps(race.getLaps());
        raceDto.setGame(race.getGame());
        raceDto.setPrivate(race.isPrivate());
        raceDto.setDescription(race.getDescription());

        model.addAttribute("race", race);
        model.addAttribute("raceDto", raceDto);
        model.addAttribute("seasons", seasonService.getActiveSeasons());
        model.addAttribute("tracks", trackService.getAllTracks());

        return "race-edit";
    }

    @PostMapping("/races/{id}/edit")
    public String updateRace(@PathVariable Long id, @Valid @ModelAttribute("raceDto") RaceDto raceDto, BindingResult result, Model model, HttpServletRequest request) {
        if (!request.isUserInRole("ROLE_ADMIN")) return "redirect:/races/" + id + "?error=BrakUprawnien";

        if (result.hasErrors()) {
            model.addAttribute("race", raceService.getRaceById(id));
            model.addAttribute("seasons", seasonService.getActiveSeasons());
            model.addAttribute("tracks", trackService.getAllTracks());
            return "race-edit";
        }

        raceService.updateRace(id, raceDto);
        return "redirect:/races/" + id + "?success=WyscigZaktualizowany";
    }
}