package tn.esprit.new_timetableservice.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tn.esprit.new_timetableservice.dto.TimeSlotDTO;
import tn.esprit.new_timetableservice.entities.School;
import tn.esprit.new_timetableservice.entities.TimeSlot;
import tn.esprit.new_timetableservice.repositories.SchoolRepository;
import tn.esprit.new_timetableservice.services.TimeSlotService;
import jakarta.validation.Valid;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/timeslots")
@Validated
public class TimeSlotController {
    private static final Logger logger = LoggerFactory.getLogger(TimeSlotController.class);
    private final TimeSlotService service;
    private final SchoolRepository schoolRepository;

    public TimeSlotController(TimeSlotService service, SchoolRepository schoolRepository) {
        this.service = service;
        this.schoolRepository = schoolRepository;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_TIME_SLOTS')")
    public ResponseEntity<TimeSlotDTO> create(@Valid @RequestBody TimeSlotDTO timeSlotDTO) {
        logger.info("Creating time slot with DTO: {}", timeSlotDTO);
        try {
            TimeSlot timeSlot = toEntity(timeSlotDTO);
            TimeSlot savedTimeSlot = service.create(timeSlot);
            TimeSlotDTO result = toDto(savedTimeSlot);
            logger.info("Time slot created successfully: {}", result);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid input: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error creating time slot: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_TIME_SLOTS')")
    public ResponseEntity<TimeSlotDTO> update(@PathVariable Long id, @Valid @RequestBody TimeSlotDTO timeSlotDTO) {
        logger.info("Updating time slot with id: {}, DTO: {}", id, timeSlotDTO);
        try {
            TimeSlot timeSlot = toEntity(timeSlotDTO);
            timeSlot.setId(id);
            TimeSlot updatedTimeSlot = service.update(id, timeSlot);
            return ResponseEntity.ok(toDto(updatedTimeSlot));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid input: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_TIME_SLOTS')")
    public ResponseEntity<TimeSlotDTO> findById(@PathVariable Long id) {
        logger.info("Fetching time slot with id: {}", id);
        TimeSlot timeSlot = service.findById(id);
        return ResponseEntity.ok(toDto(timeSlot));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_TIME_SLOTS')")
    public ResponseEntity<List<TimeSlotDTO>> findAll() {
        logger.info("Fetching all time slots");
        List<TimeSlotDTO> timeSlotDTOs = service.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(timeSlotDTOs);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_TIME_SLOTS')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logger.info("Deleting time slot with id: {}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private TimeSlotDTO toDto(TimeSlot timeSlot) {
        TimeSlotDTO dto = new TimeSlotDTO();
        dto.setId(timeSlot.getId());
        dto.setSchoolId(timeSlot.getSchool().getId());
        dto.setDay(timeSlot.getDay());
        dto.setStartTime(timeSlot.getStartTime().toString());
        dto.setEndTime(timeSlot.getEndTime().toString());
        return dto;
    }

    private TimeSlot toEntity(TimeSlotDTO dto) {
        logger.debug("Converting DTO to entity: {}", dto);
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setDay(dto.getDay());
        timeSlot.setStartTime(LocalTime.parse(dto.getStartTime()));
        timeSlot.setEndTime(LocalTime.parse(dto.getEndTime()));
        School school = schoolRepository.findById(dto.getSchoolId())
                .orElseThrow(() -> new IllegalArgumentException("School with id " + dto.getSchoolId() + " not found"));
        timeSlot.setSchool(school);
        return timeSlot;
    }
}