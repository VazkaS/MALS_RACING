package com.example.menedzeramatorskiejligisimracingowej.service;

import com.example.menedzeramatorskiejligisimracingowej.dto.UserProfileDto;
import com.example.menedzeramatorskiejligisimracingowej.dto.UserRegistrationDto;
import com.example.menedzeramatorskiejligisimracingowej.model.Role;
import com.example.menedzeramatorskiejligisimracingowej.model.User;
import com.example.menedzeramatorskiejligisimracingowej.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerUser(UserRegistrationDto registrationDto) {
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new IllegalArgumentException("Kierowca z tym adresem email już istnieje w bazie!");
        }

        if (registrationDto.getStartingNumber() != null) {
            if (userRepository.existsByStartingNumber(registrationDto.getStartingNumber())) {
                throw new IllegalArgumentException("Ten numer startowy jest już zajęty przez innego kierowcę!");
            }
        }

        User user = new User();
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setNickname(registrationDto.getNickname());
        user.setStartingNumber(registrationDto.getStartingNumber()); // Admin zapisze się po prostu z nullem
        user.setRole(registrationDto.getRole());

        userRepository.save(user);
    }

    public boolean updateUserProfile(String currentEmail, com.example.menedzeramatorskiejligisimracingowej.dto.UserProfileDto dto, org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        User user = userRepository.findByEmail(currentEmail);
        boolean credentialsChanged = false;

        user.setNickname(dto.getNickname());
        user.setStartingNumber(dto.getStartingNumber());
        user.setPreferredSimulators(dto.getPreferredSimulators());

        if (dto.getEmail() != null && !dto.getEmail().isEmpty() && !dto.getEmail().equals(currentEmail)) {
            if (userRepository.findByEmail(dto.getEmail()) == null) {
                user.setEmail(dto.getEmail());
                credentialsChanged = true;
            }
        }


        if (dto.getNewPassword() != null && !dto.getNewPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
            credentialsChanged = true;
        }

        if (dto.getProfilePicture() != null && !dto.getProfilePicture().isEmpty()) {
            try {
                byte[] imageBytes = dto.getProfilePicture().getBytes();
                String base64 = java.util.Base64.getEncoder().encodeToString(imageBytes);
                user.setProfilePicture(base64);
            } catch (Exception e) {
                System.err.println("Błąd podczas wgrywania awatara: " + e.getMessage());
            }
        }

        userRepository.save(user);
        return credentialsChanged;
    }

    // --- METODY DO OBSŁUGI PROFILU ---

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void updateUserProfile(String email, UserProfileDto profileDto) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("Użytkownik nie istnieje");
        }


        if (profileDto.getStartingNumber() != null) {
            User userWithRequestedNumber = userRepository.findByStartingNumber(profileDto.getStartingNumber());
            // Jeśli ktoś ma ten numer i NIE JEST to nasz własny profil, blokujemy:
            if (userWithRequestedNumber != null && !userWithRequestedNumber.getId().equals(user.getId())) {
                throw new IllegalArgumentException("Ten numer startowy jest już zajęty przez innego kierowcę!");
            }
        }

        user.setStartingNumber(profileDto.getStartingNumber());
        user.setPreferredSimulators(profileDto.getPreferredSimulators());

        userRepository.save(user);
    }
}