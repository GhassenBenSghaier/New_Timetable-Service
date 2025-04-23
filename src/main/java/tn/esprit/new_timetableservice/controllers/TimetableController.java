package tn.esprit.new_timetableservice.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tn.esprit.new_timetableservice.dto.*;
import tn.esprit.new_timetableservice.entities.Timetable;
import tn.esprit.new_timetableservice.repositories.*;
import tn.esprit.new_timetableservice.services.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/timetable")
@Validated
public class TimetableController {

    @Autowired private SchoolRepository schoolRepository;
    @Autowired private ProgramRepository programRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private TimetableService timetableService;
    @Autowired private CapacityService capacityService;

    private static final Logger logger = LoggerFactory.getLogger(TimetableController.class);

    @PostMapping("/capacity")
    @PreAuthorize("hasAuthority('VIEW_CAPACITY')")
    public ResponseEntity<CapacityResponse> getCapacity(@Valid @RequestBody CapacityRequest request) {
        try {
            CapacityResponse response = capacityService.calculateCapacity(request.getSchoolId(), request.getDesiredClasses());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    new CapacityResponse(request.getSchoolId(), false, null, null, null, null, null, e.getMessage())
            );
        }
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('GENERATE_TIMETABLE')")
    public ResponseEntity<TimetableDTO> generateTimetable(@Valid @RequestBody GenerateTimetableRequestDTO request) {
        try {
            TimetableDTO timetable = timetableService.generateTimetable(request);
            return ResponseEntity.ok(timetable);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("Error generating timetable: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{timetableId}")
    @PreAuthorize("hasAuthority('VIEW_TIMETABLE')")
    public ResponseEntity<TimetableDTO> getTimetable(@PathVariable Long timetableId) {
        return timetableService.getTimetableRepository()
                .findById(timetableId)
                .map(timetable -> ResponseEntity.ok(timetableService.mapToTimetableDTO(timetable)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/school/{schoolId}")
    @PreAuthorize("hasAuthority('VIEW_TIMETABLE')")
    public ResponseEntity<TimetableDTO> getTimetableBySchool(
            @PathVariable Long schoolId,
            @RequestParam(defaultValue = "Draft") String status) {
        Optional<Timetable> timetable = timetableService.getTimetableRepository()
                .findBySchoolId(schoolId);
        if (timetable.isPresent() && timetable.get().getStatus().equalsIgnoreCase(status)) {
            return ResponseEntity.ok(timetableService.mapToTimetableDTO(timetable.get()));
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{timetableId}/status")
    @PreAuthorize("hasAuthority('EDIT_TIMETABLE')")
    public ResponseEntity<TimetableDTO> updateTimetableStatus(
            @PathVariable Long timetableId,
            @RequestParam String status) {
        return timetableService.getTimetableRepository().findById(timetableId)
                .map(timetable -> {
                    timetable.setStatus(status);
                    Timetable updated = timetableService.getTimetableRepository().save(timetable);
                    return ResponseEntity.ok(timetableService.mapToTimetableDTO(updated));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/schools")
    @PreAuthorize("hasAuthority('VIEW_TIMETABLE')")
    public ResponseEntity<List<SchoolDTO>> getSchools() {
        List<SchoolDTO> schools = schoolRepository.findAll().stream()
                .map(s -> new SchoolDTO(s.getId(), s.getName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(schools);
    }

    @GetMapping("/programs")
    @PreAuthorize("hasAuthority('VIEW_TIMETABLE')")
    public ResponseEntity<List<ProgramDTO>> getPrograms(@RequestParam Long schoolId) {
        List<ProgramDTO> programs = programRepository.findAll().stream()
                .map(p -> new ProgramDTO(p.getId(), p.getName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(programs);
    }

    @GetMapping("/subjects")
    @PreAuthorize("hasAuthority('VIEW_TIMETABLE')")
    public ResponseEntity<List<SubjectDTO>> getSubjects() {
        List<SubjectDTO> subjects = subjectRepository.findAll().stream()
                .map(s -> new SubjectDTO(s.getId(), s.getName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(subjects);
    }

    public static class SchoolDTO {
        public Long id;
        public String name;
        public SchoolDTO(Long id, String name) { this.id = id; this.name = name; }
    }

    public static class ProgramDTO {
        public Long id;
        public String name;
        public ProgramDTO(Long id, String name) { this.id = id; this.name = name; }
    }

    public static class SubjectDTO {
        public Long id;
        public String name;
        public SubjectDTO(Long id, String name) { this.id = id; this.name = name; }
    }
}