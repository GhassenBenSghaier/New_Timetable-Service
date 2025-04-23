package tn.esprit.new_timetableservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.new_timetableservice.entities.TimeSlot;

import java.util.List;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    List<TimeSlot> findBySchoolId(Long schoolId);
    List<TimeSlot> findBySchoolIdAndDay(Long schoolId, String day);
}
