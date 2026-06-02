package com.example.menedzeramatorskiejligisimracingowej.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {


    @ModelAttribute("isLoggedIn")
    public boolean isLoggedIn(HttpServletRequest request) {
        return request.getUserPrincipal() != null;
    }

    @ModelAttribute("isGlobalAdmin")
    public boolean isGlobalAdmin(HttpServletRequest request) {
        return request.isUserInRole("ROLE_ADMIN");
    }


    @ModelAttribute("isGlobalDriver")
    public boolean isGlobalDriver(HttpServletRequest request) {
        return request.isUserInRole("ROLE_DRIVER");
    }
}