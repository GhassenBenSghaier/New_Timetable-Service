package tn.esprit.new_timetableservice.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tn.esprit.new_timetableservice.dto.ClassDTO;
import tn.esprit.new_timetableservice.entities.Class;
import tn.esprit.new_timetableservice.entities.School;
import tn.esprit.new_timetableservice.entities.Program;
import tn.esprit.new_timetableservice.services.ClassService;
import tn.esprit.new_timetableservice.repositories.SchoolRepository;
import tn.esprit.new_timetableservice.repositories.ProgramRepository;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/classes")
@Validated
public class ClassController {
    private static final Logger logger = LoggerFactory.getLogger(ClassController.class);
    private final ClassService service;
    private final SchoolRepository schoolRepository;
    private final ProgramRepository programRepository;

    public ClassController(ClassService service, SchoolRepository schoolRepository, ProgramRepository programRepository) {
        this.service = service;
        this.schoolRepository = schoolRepository;
        this.programRepository = programRepository;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_CLASSES')")
    public ResponseEntity<ClassDTO> create(@Valid @RequestBody ClassDTO classDTO) {
        logger.info("Creating class with DTO: {}", classDTO);
        try {
            if (classDTO.getSchoolId() == null) {
                logger.error("School ID is required");
                return ResponseEntity.badRequest().body(null);
            }
            Class classEntity = toEntity(classDTO);
            Class savedClass = service.create(classEntity);
            ClassDTO result = toDto(savedClass);
            logger.info("Class created successfully: {}", result);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid input: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("Error creating class: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_CLASSES')")
    public ResponseEntity<ClassDTO> update(@PathVariable Long id, @Valid @RequestBody ClassDTO classDTO) {
        logger.info("Updating class with id: {}, DTO: {}", id, classDTO);
        try {
            if (classDTO.getSchoolId() == null) {
                logger.error("School ID is required");
                return ResponseEntity.badRequest().body(null);
            }
            Class classEntity = toEntity(classDTO);
            classEntity.setId(id);
            Class updatedClass = service.update(id, classEntity);
            return ResponseEntity.ok(toDto(updatedClass));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid input: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("Error updating class: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_CLASSES')")
    public ResponseEntity<ClassDTO> findById(@PathVariable Long id) {
        logger.info("Fetching class with id: {}", id);
        try {
            Class classEntity = service.findById(id);
            return ResponseEntity.ok(toDto(classEntity));
        } catch (IllegalArgumentException e) {
            logger.error("Class not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_CLASSES')")
    public ResponseEntity<List<ClassDTO>> findAll() {
        logger.info("Fetching all classes");
        List<ClassDTO> classDTOs = service.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(classDTOs);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_CLASSES')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logger.info("Deleting class with id: {}", id);
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.error("Class not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/school/{schoolId}")
    @PreAuthorize("hasAuthority('MANAGE_CLASSES')")
    public ResponseEntity<List<ClassDTO>> findBySchoolId(@PathVariable Long schoolId) {
        logger.info("Fetching classes for school with id: {}", schoolId);
        try {
            List<ClassDTO> classDTOs = service.findBySchoolId(schoolId)
                    .stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(classDTOs);
        } catch (Exception e) {
            logger.error("Error fetching classes for school: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private ClassDTO toDto(Class classEntity) {
        ClassDTO dto = new ClassDTO();
        dto.setId(classEntity.getId());
        dto.setSchoolId(classEntity.getSchool() != null ? classEntity.getSchool().getId() : null);
        dto.setProgramId(classEntity.getProgram() != null ? classEntity.getProgram().getId() : null);
        dto.setName(classEntity.getName());
        dto.setStudentCount(classEntity.getStudentCount());
        return dto;
    }

    private Class toEntity(ClassDTO dto) {
        logger.debug("Converting DTO to entity: {}", dto);
        Class classEntity = new Class();
        classEntity.setName(dto.getName());
        classEntity.setStudentCount(dto.getStudentCount());
        if (dto.getSchoolId() == null) {
            throw new IllegalArgumentException("School ID is required");
        }
        School school = schoolRepository.findById(dto.getSchoolId())
                .orElseThrow(() -> new IllegalArgumentException("School with id " + dto.getSchoolId() + " not found"));
        classEntity.setSchool(school);
        if (dto.getProgramId() == null) {
            throw new IllegalArgumentException("Program ID is required");
        }
        Program program = programRepository.findById(dto.getProgramId())
                .orElseThrow(() -> new IllegalArgumentException("Program with id " + dto.getProgramId() + " not found"));
        classEntity.setProgram(program);
        return classEntity;
    }
}