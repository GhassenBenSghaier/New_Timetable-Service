package tn.esprit.new_timetableservice.services;

import org.springframework.stereotype.Service;
import tn.esprit.new_timetableservice.entities.TimeSlot;
import tn.esprit.new_timetableservice.repositories.TimeSlotRepository;

import java.time.LocalTime;
import java.util.List;

@Service
public class TimeSlotService {
    private final TimeSlotRepository repository;

    public TimeSlotService(TimeSlotRepository repository) {
        this.repository = repository;
    }

    public TimeSlot create(TimeSlot timeSlot) {
        // Validate for overlaps
        if (hasOverlap(timeSlot)) {
            throw new IllegalArgumentException("Time slot overlaps with an existing time slot for the same school and day");
        }
        return repository.save(timeSlot);
    }

    public TimeSlot update(Long id, TimeSlot timeSlot) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("TimeSlot with id " + id + " not found");
        }
        timeSlot.setId(id);
        // Validate for overlaps, excluding the current time slot
        if (hasOverlap(timeSlot, id)) {
            throw new IllegalArgumentException("Updated time slot overlaps with an existing time slot for the same school and day");
        }
        return repository.save(timeSlot);
    }

    public TimeSlot findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("TimeSlot with id " + id + " not found"));
    }

    public List<TimeSlot> findAll() {
        return repository.findAll();
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("TimeSlot with id " + id + " not found");
        }
        repository.deleteById(id);
    }

    public List<TimeSlot> findBySchoolId(Long schoolId) {
        return repository.findBySchoolId(schoolId);
    }

    public List<TimeSlot> findBySchoolIdAndDay(Long schoolId, String day) {
        return repository.findBySchoolIdAndDay(schoolId, day);
    }

    private boolean hasOverlap(TimeSlot newTimeSlot) {
        return hasOverlap(newTimeSlot, null);
    }

    private boolean hasOverlap(TimeSlot newTimeSlot, Long excludeId) {
        List<TimeSlot> existingTimeSlots = repository.findBySchoolIdAndDay(
                newTimeSlot.getSchool().getId(),
                newTimeSlot.getDay()
        );

        LocalTime newStart = newTimeSlot.getStartTime();
        LocalTime newEnd = newTimeSlot.getEndTime();

        for (TimeSlot existing : existingTimeSlots) {
            // Skip the time slot being updated
            if (excludeId != null && existing.getId().equals(excludeId)) {
                continue;
            }

            LocalTime existingStart = existing.getStartTime();
            LocalTime existingEnd = existing.getEndTime();

            // Check for overlap:
            // Overlap occurs if newStart or newEnd falls within existingStart and existingEnd,
            // or if existingStart or existingEnd falls within newStart and newEnd
            if (!(newEnd.isBefore(existingStart) || newStart.isAfter(existingEnd))) {
                return true;
            }
        }
        return false;
    }
}