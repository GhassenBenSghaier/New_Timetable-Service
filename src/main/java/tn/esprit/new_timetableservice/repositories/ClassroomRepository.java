package tn.esprit.new_timetableservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.new_timetableservice.entities.Classroom;

import java.util.List;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    List<Classroom> findBySchoolId(Long schoolId);
    List<Classroom> findBySchoolIdAndType(Long schoolId, String type);
}