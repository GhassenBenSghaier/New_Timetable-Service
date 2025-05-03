package tn.esprit.new_timetableservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.new_timetableservice.entities.Subject;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    Optional<Subject> findByName(String name);
    List<Subject> findAll();
}
