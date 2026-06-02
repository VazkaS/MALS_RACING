package com.example.menedzeramatorskiejligisimracingowej.repository;

import com.example.menedzeramatorskiejligisimracingowej.model.Race;
import com.example.menedzeramatorskiejligisimracingowej.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RaceRepository extends JpaRepository<Race, Long> {


    List<Race> findAllByArchivedFalseOrderByRaceDateAsc();


    List<Race> findAllByArchivedTrueOrderByRaceDateDesc();

    List<Race> findBySignedUpDriversContainingAndArchivedFalseOrderByRaceDateAsc(User user);
}