package com.example.menedzeramatorskiejligisimracingowej.repository;

import com.example.menedzeramatorskiejligisimracingowej.model.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SeasonRepository extends JpaRepository<Season, Long> {


    List<Season> findAllByArchivedTrue();


    List<Season> findAllByArchivedFalseAndActiveTrue();


    List<Season> findAllByArchivedFalseAndActiveFalse();
}