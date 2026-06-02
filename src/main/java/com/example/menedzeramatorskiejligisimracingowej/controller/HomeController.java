package com.example.menedzeramatorskiejligisimracingowej.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {


    @GetMapping("/")
    public String home(Model model, Authentication auth) {

        boolean isLoggedIn = auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());
        model.addAttribute("isLoggedIn", isLoggedIn);

        if (isLoggedIn) {
            boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("ADMIN"));
            boolean isDriver = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("DRIVER") || a.getAuthority().contains("USER"));

            model.addAttribute("isGlobalAdmin", isAdmin);
            model.addAttribute("isGlobalDriver", isDriver);
        }

        return "home";
    }
}