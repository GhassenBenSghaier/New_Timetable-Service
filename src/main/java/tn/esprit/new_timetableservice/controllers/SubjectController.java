package tn.esprit.new_timetableservice.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tn.esprit.new_timetableservice.dto.SubjectDTO;
import tn.esprit.new_timetableservice.entities.Subject;
import tn.esprit.new_timetableservice.services.SubjectService;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/subjects")
@Validated
public class SubjectController {
    private static final Logger logger = LoggerFactory.getLogger(SubjectController.class);
    private final SubjectService service;

    public SubjectController(SubjectService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SubjectDTO> create(@Valid @RequestBody SubjectDTO subjectDTO) {
        logger.info("Creating subject with DTO: {}", subjectDTO);
        Subject subject = toEntity(subjectDTO);
        Subject savedSubject = service.create(subject);
        return ResponseEntity.ok(toDto(savedSubject));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubjectDTO> update(@PathVariable Long id, @Valid @RequestBody SubjectDTO subjectDTO) {
        logger.info("Updating subject with id: {}, DTO: {}", id, subjectDTO);
        Subject subject = toEntity(subjectDTO);
        subject.setId(id);
        Subject updatedSubject = service.update(id, subject);
        return ResponseEntity.ok(toDto(updatedSubject));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubjectDTO> findById(@PathVariable Long id) {
        logger.info("Fetching subject with id: {}", id);
        Subject subject = service.findById(id);
        return ResponseEntity.ok(toDto(subject));
    }

    @GetMapping
    public ResponseEntity<List<SubjectDTO>> findAll() {
        logger.info("Fetching all subjects");
        List<SubjectDTO> subjectDTOs = service.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(subjectDTOs);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logger.info("Deleting subject with id: {}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private SubjectDTO toDto(Subject subject) {
        SubjectDTO dto = new SubjectDTO();
        dto.setId(subject.getId());
        dto.setName(subject.getName());
        dto.setDefaultHoursPerWeek(subject.getDefaultHoursPerWeek());
        dto.setRoomType(subject.getRoomType());
        return dto;
    }

    private Subject toEntity(SubjectDTO dto) {
        Subject subject = new Subject();
        subject.setName(dto.getName());
        subject.setDefaultHoursPerWeek(dto.getDefaultHoursPerWeek());
        subject.setRoomType(dto.getRoomType());
        return subject;
    }
}