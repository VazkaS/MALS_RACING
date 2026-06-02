package com.example.menedzeramatorskiejligisimracingowej.controller;

import com.example.menedzeramatorskiejligisimracingowej.dto.UserRegistrationDto;
import com.example.menedzeramatorskiejligisimracingowej.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegistrationController {

    private final UserService userService;


    private final String SECRET_ADMIN_CODE = "admin";

    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUserAccount(@Valid @ModelAttribute("user") UserRegistrationDto registrationDto,
                                      BindingResult result) {


        if (com.example.menedzeramatorskiejligisimracingowej.model.Role.ADMIN.equals(registrationDto.getRole())) {
            if (registrationDto.getAdminCode() == null || !registrationDto.getAdminCode().equals(SECRET_ADMIN_CODE)) {
                return "redirect:/register?error=badAdminCode";
            }
        }

        if (com.example.menedzeramatorskiejligisimracingowej.model.Role.DRIVER.equals(registrationDto.getRole())) {
            if (registrationDto.getStartingNumber() == null) {
                result.rejectValue("startingNumber", "user.startingNumber", "Numer startowy jest wymagany dla kierowcy!");
            }
        }

        if (result.hasErrors()) {
            return "register";
        }

        try {
            userService.registerUser(registrationDto);
        } catch (IllegalArgumentException e) {
            result.rejectValue("email", "user.email", e.getMessage());
            return "register";
        }

        return "redirect:/login?success";
    }
}