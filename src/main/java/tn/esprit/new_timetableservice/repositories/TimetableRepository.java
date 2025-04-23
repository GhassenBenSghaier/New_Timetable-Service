package tn.esprit.new_timetableservice.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import tn.esprit.new_timetableservice.entities.Timetable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TimetableRepository extends JpaRepository<Timetable, Long> {
    Optional<Timetable> findBySchoolIdAndStatus(Long schoolId, String status);
    Optional<Timetable> findBySchoolId(Long schoolId);
    @Modifying
    @Query("DELETE FROM Timetable t WHERE t.school.id = :schoolId")
    int deleteBySchoolId(Long schoolId);
}