package tn.esprit.new_timetableservice.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tn.esprit.new_timetableservice.dto.SpecialtyDTO;
import tn.esprit.new_timetableservice.entities.Level;
import tn.esprit.new_timetableservice.entities.Specialty;
import tn.esprit.new_timetableservice.services.SpecialtyService;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/specialties")
@Validated
public class SpecialtyController {
    private static final Logger logger = LoggerFactory.getLogger(SpecialtyController.class);
    private final SpecialtyService service;

    public SpecialtyController(SpecialtyService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SpecialtyDTO> create(@Valid @RequestBody SpecialtyDTO specialtyDTO) {
        logger.info("Creating specialty with DTO: {}", specialtyDTO);
        Specialty specialty = toEntity(specialtyDTO);
        Specialty savedSpecialty = service.create(specialty);
        return ResponseEntity.ok(toDto(savedSpecialty));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SpecialtyDTO> update(@PathVariable Long id, @Valid @RequestBody SpecialtyDTO specialtyDTO) {
        logger.info("Updating specialty with id: {}, DTO: {}", id, specialtyDTO);
        Specialty specialty = toEntity(specialtyDTO);
        specialty.setId(id);
        Specialty updatedSpecialty = service.update(id, specialty);
        return ResponseEntity.ok(toDto(updatedSpecialty));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpecialtyDTO> findById(@PathVariable Long id) {
        logger.info("Fetching specialty with id: {}", id);
        Specialty specialty = service.findById(id);
        return ResponseEntity.ok(toDto(specialty));
    }

    @GetMapping
    public ResponseEntity<List<SpecialtyDTO>> findAll() {
        logger.info("Fetching all specialties");
        List<SpecialtyDTO> specialtyDTOs = service.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(specialtyDTOs);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logger.info("Deleting specialty with id: {}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private SpecialtyDTO toDto(Specialty specialty) {
        SpecialtyDTO dto = new SpecialtyDTO();
        dto.setId(specialty.getId());
        dto.setName(specialty.getName());
        dto.setLevelId(specialty.getLevel().getId());
        return dto;
    }

    private Specialty toEntity(SpecialtyDTO dto) {
        Specialty specialty = new Specialty();
        specialty.setName(dto.getName());
        Level level = new Level();
        level.setId(dto.getLevelId());
        specialty.setLevel(level);
        return specialty;
    }
}