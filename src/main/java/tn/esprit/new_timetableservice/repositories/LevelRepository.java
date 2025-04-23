package tn.esprit.new_timetableservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.new_timetableservice.entities.Level;

import java.util.List;

public interface LevelRepository extends JpaRepository<Level, Long> {
    List<Level> findBySchoolId(Long schoolId);
}

