package tn.esprit.new_timetableservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.new_timetableservice.entities.Teacher;
import tn.esprit.new_timetableservice.entities.Subject;
import tn.esprit.new_timetableservice.entities.Program;
import java.util.List;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    List<Teacher> findBySubjectAndProgramsContaining(Subject subject, Program program);
    List<Teacher> findBySubjectIdAndSchoolId(Long subjectId, Long schoolId);
    List<Teacher> findBySchoolId(Long schoolId);
}