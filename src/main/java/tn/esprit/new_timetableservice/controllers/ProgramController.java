package tn.esprit.new_timetableservice.controllers;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tn.esprit.new_timetableservice.dto.*;
import tn.esprit.new_timetableservice.services.ProgramService;
import org.springframework.dao.DataIntegrityViolationException;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/program")
@Validated
public class ProgramController {

    @Autowired
    private ProgramService programService;

    @PostMapping
    @PreAuthorize("hasAuthority('ADD_PROGRAM')")
    public ResponseEntity<ProgramDTO> createProgram(@Valid @RequestBody ProgramDTO request) {
        try {
            ProgramDTO program = programService.createProgram(request);
            return ResponseEntity.ok(program);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EDIT_PROGRAM')")
    public ResponseEntity<ProgramDTO> updateProgram(@PathVariable Long id, @Valid @RequestBody ProgramDTO request) {
        try {
            request.setId(id);
            ProgramDTO program = programService.updateProgram(request);
            return ResponseEntity.ok(program);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_PROGRAM')")
    public ResponseEntity<List<ProgramDTO>> getPrograms() {
        try {
            List<ProgramDTO> programs = programService.getPrograms();
            return ResponseEntity.ok(programs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_PROGRAM')")
    public ResponseEntity<ProgramDTO> getProgramById(@PathVariable Long id) {
        try {
            ProgramDTO program = programService.getProgramById(id);
            return ResponseEntity.ok(program);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_PROGRAM')")
    public ResponseEntity<Void> deleteProgram(@PathVariable Long id) {
        try {
            programService.deleteProgram(id);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/by-name/{name}")
    @PreAuthorize("hasAuthority('VIEW_PROGRAM')")
    public ResponseEntity<ProgramDTO> getProgramByName(@PathVariable String name) {
        try {
            ProgramDTO program = programService.getProgramByName(name);
            return ResponseEntity.ok(program);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/level")
    @PreAuthorize("hasAuthority('VIEW_PROGRAM')")
    public ResponseEntity<List<LevelDTO>> getLevels() {
        try {
            List<LevelDTO> levels = programService.getLevels();
            return ResponseEntity.ok(levels);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/specialty/{levelId}")
    @PreAuthorize("hasAuthority('VIEW_PROGRAM')")
    public ResponseEntity<List<SpecialtyDTO>> getSpecialtiesByLevel(@PathVariable Long levelId) {
        try {
            List<SpecialtyDTO> specialties = programService.getSpecialtiesByLevel(levelId);
            return ResponseEntity.ok(specialties);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/subject")
    @PreAuthorize("hasAuthority('VIEW_PROGRAM')")
    public ResponseEntity<List<SubjectDTO>> getSubjects() {
        try {
            List<SubjectDTO> subjects = programService.getSubjects();
            return ResponseEntity.ok(subjects);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}