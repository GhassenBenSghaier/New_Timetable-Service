package tn.esprit.new_timetableservice.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tn.esprit.new_timetableservice.dto.ClassroomDTO;
import tn.esprit.new_timetableservice.entities.Classroom;
import tn.esprit.new_timetableservice.entities.School;
import tn.esprit.new_timetableservice.services.ClassroomService;
import tn.esprit.new_timetableservice.repositories.SchoolRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/classrooms")
@Validated
public class ClassroomController {
    private static final Logger logger = LoggerFactory.getLogger(ClassroomController.class);
    private final ClassroomService service;
    private final SchoolRepository schoolRepository;

    public ClassroomController(ClassroomService service, SchoolRepository schoolRepository) {
        this.service = service;
        this.schoolRepository = schoolRepository;
    }

    @PostMapping
    public ResponseEntity<ClassroomDTO> create(@Valid @RequestBody ClassroomDTO classroomDTO) {
        logger.info("Creating classroom with DTO: {}", classroomDTO);
        try {
            Classroom classroom = toEntity(classroomDTO);
            Classroom savedClassroom = service.create(classroom);
            ClassroomDTO result = toDto(savedClassroom);
            logger.info("Classroom created successfully: {}", result);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid input: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error creating classroom: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClassroomDTO> update(@PathVariable Long id, @Valid @RequestBody ClassroomDTO classroomDTO) {
        logger.info("Updating classroom with id: {}, DTO: {}", id, classroomDTO);
        try {
            Classroom classroom = toEntity(classroomDTO);
            classroom.setId(id);
            Classroom updatedClassroom = service.update(id, classroom);
            return ResponseEntity.ok(toDto(updatedClassroom));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid input: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassroomDTO> findById(@PathVariable Long id) {
        logger.info("Fetching classroom with id: {}", id);
        Classroom classroom = service.findById(id);
        return ResponseEntity.ok(toDto(classroom));
    }

    @GetMapping
    public ResponseEntity<List<ClassroomDTO>> findAll() {
        logger.info("Fetching all classrooms");
        List<ClassroomDTO> classroomDTOs = service.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(classroomDTOs);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logger.info("Deleting classroom with id: {}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private ClassroomDTO toDto(Classroom classroom) {
        ClassroomDTO dto = new ClassroomDTO();
        dto.setId(classroom.getId());
        dto.setSchoolId(classroom.getSchool().getId());
        dto.setName(classroom.getName());
        dto.setCapacity(classroom.getCapacity());
        dto.setType(classroom.getType());
        return dto;
    }

    private Classroom toEntity(ClassroomDTO dto) {
        logger.debug("Converting DTO to entity: {}", dto);
        Classroom classroom = new Classroom();
        classroom.setName(dto.getName());
        classroom.setCapacity(dto.getCapacity());
        classroom.setType(dto.getType());
        School school = schoolRepository.findById(dto.getSchoolId())
                .orElseThrow(() -> new IllegalArgumentException("School with id " + dto.getSchoolId() + " not found"));
        classroom.setSchool(school);
        // Do not set ID for new entities to allow database auto-generation
        return classroom;
    }
}