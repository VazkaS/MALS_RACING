package com.example.menedzeramatorskiejligisimracingowej.repository;

import com.example.menedzeramatorskiejligisimracingowej.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; // <--- TEGO IMPORTU BRAKOWAŁO

@Repository
public interface UserRepository extends JpaRepository<User, Long> {


    boolean existsByEmail(String email);

    User findByEmail(String email);

    List<User> findByRole(com.example.menedzeramatorskiejligisimracingowej.model.Role role);

    List<User> findByNicknameContainingIgnoreCaseAndRole(String keyword, com.example.menedzeramatorskiejligisimracingowej.model.Role role);

    boolean existsByStartingNumber(Integer startingNumber);

    User findByStartingNumber(Integer startingNumber);
}