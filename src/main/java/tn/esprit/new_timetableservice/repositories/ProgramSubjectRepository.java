package tn.esprit.new_timetableservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.new_timetableservice.entities.ProgramSubject;

import java.util.List;

public interface ProgramSubjectRepository extends JpaRepository<ProgramSubject, Long> {
    List<ProgramSubject> findByProgramId(Long programId);
}