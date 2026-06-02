package com.example.menedzeramatorskiejligisimracingowej.controller;

import com.example.menedzeramatorskiejligisimracingowej.dto.UserProfileDto;
import com.example.menedzeramatorskiejligisimracingowej.model.User;
import com.example.menedzeramatorskiejligisimracingowej.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String showProfile(Model model, Principal principal) {
        User user = userService.findByEmail(principal.getName());

        UserProfileDto dto = new UserProfileDto();
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setStartingNumber(user.getStartingNumber());
        dto.setPreferredSimulators(user.getPreferredSimulators());

        model.addAttribute("user", user);
        model.addAttribute("profileDto", dto);
        return "profile";
    }

    @PostMapping
    public String updateProfile(@ModelAttribute("profileDto") UserProfileDto dto, Principal principal, HttpServletRequest request) {
        try {
            boolean credentialsChanged = userService.updateUserProfile(principal.getName(), dto, passwordEncoder);

            if (credentialsChanged) {
                try {
                    request.logout(); // Wylogowanie po zmianie maila/hasła
                } catch (Exception e) {
                    System.err.println("Błąd wylogowywania: " + e.getMessage());
                }
                return "redirect:/login?logout=true&message=ZalogujSiePonownie";
            }

            return "redirect:/profile?success";

        } catch (DataIntegrityViolationException e) {
            return "redirect:/profile?error=numerZajety";
        }
    }
}