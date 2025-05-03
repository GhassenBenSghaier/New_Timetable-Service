package tn.esprit.new_timetableservice.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tn.esprit.new_timetableservice.dto.LevelDTO;
import tn.esprit.new_timetableservice.entities.Level;
import tn.esprit.new_timetableservice.entities.School;
import tn.esprit.new_timetableservice.services.LevelService;
import tn.esprit.new_timetableservice.services.SchoolService;
import jakarta.validation.Valid;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/levels")
@Validated
public class LevelController {
    private static final Logger logger = LoggerFactory.getLogger(LevelController.class);
    private final LevelService levelService;
    private final SchoolService schoolService;

    public LevelController(LevelService levelService, SchoolService schoolService) {
        this.levelService = levelService;
        this.schoolService = schoolService;
        logger.info("LevelController initialized");
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_LEVELS')")
    public ResponseEntity<LevelDTO> create(@Valid @RequestBody LevelDTO levelDTO) {
        logger.info("Creating level with DTO: {}", levelDTO);
        Level level = toEntity(levelDTO);
        Level savedLevel = levelService.create(level);
        return ResponseEntity.ok(toDto(savedLevel));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_LEVELS')")
    public ResponseEntity<LevelDTO> update(@PathVariable Long id, @Valid @RequestBody LevelDTO levelDTO) {
        logger.info("Updating level with id: {}, DTO: {}", id, levelDTO);
        Level level = toEntity(levelDTO);
        level.setId(id);
        Level updatedLevel = levelService.update(id, level);
        return ResponseEntity.ok(toDto(updatedLevel));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_LEVELS')")
    public ResponseEntity<LevelDTO> findById(@PathVariable Long id) {
        logger.info("Fetching level with id: {}", id);
        Level level = levelService.findById(id);
        return ResponseEntity.ok(toDto(level));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_LEVELS')")
    public ResponseEntity<List<LevelDTO>> findAll() {
        logger.info("Fetching all levels");
        List<LevelDTO> levelDTOs = levelService.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(levelDTOs);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_LEVELS')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logger.info("Deleting level with id: {}", id);
        levelService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private LevelDTO toDto(Level level) {
        LevelDTO dto = new LevelDTO();
        dto.setId(level.getId());
        dto.setName(level.getName());
        dto.setSupportsSpecialty(level.isSupportsSpecialty());
        return dto;
    }

    private Level toEntity(LevelDTO dto) {
        Level level = new Level();
        level.setName(dto.getName());
        level.setSupportsSpecialty(dto.isSupportsSpecialty());
        return level;
    }
}