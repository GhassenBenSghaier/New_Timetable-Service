package tn.esprit.new_timetableservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.new_timetableservice.entities.Program;

import java.util.List;
import java.util.Optional;

public interface ProgramRepository extends JpaRepository<Program, Long> {
    Optional<Program> findByName(String name);


}