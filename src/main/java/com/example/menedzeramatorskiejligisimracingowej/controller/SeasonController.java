package com.example.menedzeramatorskiejligisimracingowej.controller;

import com.example.menedzeramatorskiejligisimracingowej.dto.SeasonDto;
import com.example.menedzeramatorskiejligisimracingowej.model.Season;
import com.example.menedzeramatorskiejligisimracingowej.service.SeasonService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/seasons")
public class SeasonController {

    private final SeasonService seasonService;

    public SeasonController(SeasonService seasonService) {
        this.seasonService = seasonService;
    }

    @GetMapping
    public String showSeasons(Model model, HttpServletRequest request) {
        boolean isAdmin = request.isUserInRole("ROLE_ADMIN");
        model.addAttribute("isAdmin", isAdmin);


        model.addAttribute("activeSeasons", seasonService.getActiveSeasons());
        model.addAttribute("upcomingSeasons", seasonService.getUpcomingSeasons()); // NOWA LISTA
        model.addAttribute("archivedSeasons", seasonService.getArchivedSeasons());

        if (isAdmin) {
            model.addAttribute("newSeason", new SeasonDto());
        }
        return "seasons";
    }

    @PostMapping
    public String addSeason(@Valid @ModelAttribute("newSeason") SeasonDto seasonDto, BindingResult result, Model model, HttpServletRequest request) {
        if (!request.isUserInRole("ROLE_ADMIN")) {
            return "redirect:/seasons?error=BrakUprawnien";
        }



        if (result.hasErrors()) {
            model.addAttribute("isAdmin", true);
            model.addAttribute("activeSeasons", seasonService.getActiveSeasons());
            model.addAttribute("upcomingSeasons", seasonService.getUpcomingSeasons()); // NOWA LISTA
            model.addAttribute("archivedSeasons", seasonService.getArchivedSeasons());
            return "seasons";
        }

        seasonService.createSeason(seasonDto);
        return "redirect:/seasons?success=true";
    }

    @GetMapping("/points-system")
    public String showPointsSystem() {
        return "points-system";
    }

    @GetMapping("/{id}")
    public String showSeasonLeaderboards(@PathVariable Long id, Model model, HttpServletRequest request) {
        Season season = seasonService.getSeasonById(id);

        model.addAttribute("season", season);
        model.addAttribute("isAdmin", request.isUserInRole("ROLE_ADMIN"));

        model.addAttribute("acStandings", seasonService.getSeasonStandings(season, "Assetto Corsa"));
        model.addAttribute("acEvoStandings", seasonService.getSeasonStandings(season, "Assetto Corsa Evo"));
        model.addAttribute("accStandings", seasonService.getSeasonStandings(season, "Assetto Corsa Competizione"));
        model.addAttribute("iracingStandings", seasonService.getSeasonStandings(season, "iRacing"));

        return "season-details";
    }

    @PostMapping("/{id}/archive")
    public String archiveSeason(@PathVariable Long id, HttpServletRequest request) {
        if (!request.isUserInRole("ROLE_ADMIN")) {
            return "redirect:/seasons/" + id + "?error=BrakUprawnien";
        }

        seasonService.archiveSeason(id);
        return "redirect:/seasons/" + id + "?success=SezonZakonczony";
    }

    @PostMapping("/{id}/activate")
    public String activateSeason(@PathVariable Long id, HttpServletRequest request) {
        if (!request.isUserInRole("ROLE_ADMIN")) {
            return "redirect:/seasons/" + id + "?error=BrakUprawnien";
        }

        seasonService.activateSeason(id);
        return "redirect:/seasons/" + id + "?success=SezonAktywowany";
    }
}