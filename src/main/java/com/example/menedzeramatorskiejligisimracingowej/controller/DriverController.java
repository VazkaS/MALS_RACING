package com.example.menedzeramatorskiejligisimracingowej.controller;

import com.example.menedzeramatorskiejligisimracingowej.dto.DriverRowDto;
import com.example.menedzeramatorskiejligisimracingowej.model.RaceResult;
import com.example.menedzeramatorskiejligisimracingowej.model.Role;
import com.example.menedzeramatorskiejligisimracingowej.model.User;
import com.example.menedzeramatorskiejligisimracingowej.repository.UserRepository;
import com.example.menedzeramatorskiejligisimracingowej.service.DriverService;
import com.example.menedzeramatorskiejligisimracingowej.service.RaceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/drivers")
public class DriverController {

    private final UserRepository userRepository;
    private final DriverService driverService;
    private final RaceService raceService;

    public DriverController(UserRepository userRepository, DriverService driverService, RaceService raceService) {
        this.userRepository = userRepository;
        this.driverService = driverService;
        this.raceService = raceService;
    }

    @GetMapping
    public String showDrivers(Model model,
                              @RequestParam(required = false) String simulator,
                              @RequestParam(required = false) String keyword,
                              HttpServletRequest request) { // <-- Dodano request dla ról

        List<User> drivers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.DRIVER)
                .collect(Collectors.toList());

        if (keyword != null && !keyword.trim().isEmpty()) {
            String lowerKeyword = keyword.toLowerCase();
            drivers = drivers.stream()
                    .filter(u -> u.getNickname().toLowerCase().contains(lowerKeyword))
                    .collect(Collectors.toList());
            model.addAttribute("keyword", keyword);
        }

        if (simulator != null && !simulator.isEmpty()) {
            drivers = drivers.stream()
                    .filter(u -> u.getPreferredSimulators() != null && u.getPreferredSimulators().contains(simulator))
                    .collect(Collectors.toList());
            model.addAttribute("selectedSim", simulator);
        }

        List<DriverRowDto> driverRows = new ArrayList<>();
        for (User driver : drivers) {
            DriverRowDto row = new DriverRowDto();
            row.setDriver(driver); // Zapisujemy jako .driver (ważne do HTML)

            List<RaceResult> history = driverService.getDriverRaceHistory(driver);
            row.setStats(driverService.calculateDriverStats(driver, history));

            driverRows.add(row);
        }

        // Sortowanie po punktach
        driverRows.sort((r1, r2) -> Integer.compare(r2.getStats().getTotalPoints(), r1.getStats().getTotalPoints()));

        // KLUCZOWA ZMIANA: Zmieniono nazwę atrybutu z "driverRows" na "drivers" dla nowego HTML
        model.addAttribute("drivers", driverRows);
        model.addAttribute("isAdmin", request.isUserInRole("ROLE_ADMIN"));

        return "drivers";
    }

    @GetMapping("/{id}")
    public String showDriverProfile(@PathVariable Long id, Model model, HttpServletRequest request) {
        User driver = driverService.getDriverById(id);
        List<RaceResult> history = driverService.getDriverRaceHistory(driver);

        model.addAttribute("driver", driver);
        model.addAttribute("history", history);
        model.addAttribute("stats", driverService.calculateDriverStats(driver, history));
        model.addAttribute("upcomingRaces", raceService.getDriverUpcomingRaces(driver));
        model.addAttribute("isAdmin", request.isUserInRole("ROLE_ADMIN"));

        return "driver-details";
    }
}