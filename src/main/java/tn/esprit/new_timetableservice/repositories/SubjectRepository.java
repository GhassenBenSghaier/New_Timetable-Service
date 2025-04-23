package tn.esprit.new_timetableservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.new_timetableservice.entities.Subject;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    Subject findByName(String name);
    List<Subject> findAll();
}
