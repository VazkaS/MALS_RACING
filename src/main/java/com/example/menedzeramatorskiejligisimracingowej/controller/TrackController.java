package com.example.menedzeramatorskiejligisimracingowej.controller;

import com.example.menedzeramatorskiejligisimracingowej.dto.LapTimeDto;
import com.example.menedzeramatorskiejligisimracingowej.dto.TrackDto;
import com.example.menedzeramatorskiejligisimracingowej.model.Track;
import com.example.menedzeramatorskiejligisimracingowej.service.TrackService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException; // <--- DODANY IMPORT
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/tracks")
public class TrackController {

    private final TrackService trackService;

    public TrackController(TrackService trackService) {
        this.trackService = trackService;
    }

    @GetMapping
    public String showTrackList(Model model, HttpServletRequest request) {
        boolean isAdmin = request.isUserInRole("ROLE_ADMIN");
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("tracks", trackService.getAllTracks());

        if (isAdmin) {
            model.addAttribute("newTrack", new TrackDto());
        }
        return "tracks";
    }

    @PostMapping
    public String addTrack(@Valid @ModelAttribute("newTrack") TrackDto dto,
                           BindingResult result,
                           HttpServletRequest request) {
        if (!request.isUserInRole("ROLE_ADMIN")) {
            return "redirect:/tracks?error=BrakUprawnien";
        }
        if (result.hasErrors()) {
            return "redirect:/tracks?error=BledneDane";
        }
        trackService.createTrack(dto);
        return "redirect:/tracks?success=TorDodany";
    }


    @PostMapping("/{id}/delete")
    public String deleteTrack(@PathVariable Long id, HttpServletRequest request) {
        if (!request.isUserInRole("ROLE_ADMIN")) {
            return "redirect:/tracks?error=BrakUprawnien";
        }

        try {
            trackService.deleteTrack(id);
            return "redirect:/tracks?deleted=true";
        } catch (DataIntegrityViolationException e) {
            // Przechwytujemy błąd bazy danych, jeśli tor jest przypisany do wyścigu
            return "redirect:/tracks?error=TorAktywny";
        }
    }

    @GetMapping("/{id}")
    public String showTrackLeaderboards(@PathVariable Long id, Model model) {
        Track track = trackService.getTrackById(id);

        model.addAttribute("track", track);

        model.addAttribute("acTimes", trackService.getLeaderboard(track, "Assetto Corsa"));
        model.addAttribute("acEvoTimes", trackService.getLeaderboard(track, "Assetto Corsa Evo"));
        model.addAttribute("accTimes", trackService.getLeaderboard(track, "Assetto Corsa Competizione"));
        model.addAttribute("iracingTimes", trackService.getLeaderboard(track, "iRacing"));

        return "track-details";
    }
}