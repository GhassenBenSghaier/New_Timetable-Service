package tn.esprit.new_timetableservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.new_timetableservice.entities.TeacherAvailability;

import java.util.List;

public interface TeacherAvailabilityRepository extends JpaRepository<TeacherAvailability, Long> {
    List<TeacherAvailability> findByTeacherId(Long teacherId);
    List<TeacherAvailability> findByTeacherIdAndIsAvailable(Long teacherId, Boolean isAvailable);
}
