package tn.esprit.new_timetableservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import tn.esprit.new_timetableservice.entities.Schedule;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByTimetableId(Long timetableId);
    @Modifying
    @Query("DELETE FROM Schedule s WHERE s.timetable.school.id = :schoolId")
    int deleteByTimetableSchoolId(Long schoolId);
}