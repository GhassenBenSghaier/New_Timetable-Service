//package tn.esprit.new_timetableservice.controllers;
//
//import jakarta.persistence.EntityNotFoundException;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.*;
//import tn.esprit.new_timetableservice.dto.*;
//import tn.esprit.new_timetableservice.services.ProgramService;
//import org.springframework.dao.DataIntegrityViolationException;
//
//import jakarta.validation.Valid;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/v1/program")
//@Validated
//public class ProgramController {
//
//    @Autowired
//    private ProgramService programService;
//
//    @PostMapping
//    @PreAuthorize("hasAuthority('ADD_PROGRAM')")
//    public ResponseEntity<ProgramDTO> createProgram(@Valid @RequestBody ProgramDTO request) {
//        try {
//            ProgramDTO program = programService.createProgram(request);
//            return ResponseEntity.ok(program);
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().body(null);
//        } catch (DataIntegrityViolationException e) {
//            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//        }
//    }
//
//    @PutMapping("/{id}")
//    @PreAuthorize("hasAuthority('EDIT_PROGRAM')")
//    public ResponseEntity<ProgramDTO> updateProgram(@PathVariable Long id, @Valid @RequestBody ProgramDTO request) {
//        try {
//            request.setId(id);
//            ProgramDTO program = programService.updateProgram(request);
//            return ResponseEntity.ok(program);
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().body(null);
//        } catch (EntityNotFoundException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//        }
//    }
//
//    @GetMapping
//    @PreAuthorize("hasAuthority('VIEW_PROGRAM')")
//    public ResponseEntity<List<ProgramDTO>> getPrograms() {
//        try {
//            List<ProgramDTO> programs = programService.getPrograms();
//            return ResponseEntity.ok(programs);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//        }
//    }
//
//    @GetMapping("/{id}")
//    @PreAuthorize("hasAuthority('VIEW_PROGRAM')")
//    public ResponseEntity<ProgramDTO> getProgramById(@PathVariable Long id) {
//        try {
//            ProgramDTO program = programService.getProgramById(id);
//            return ResponseEntity.ok(program);
//        } catch (EntityNotFoundException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//        }
//    }
//
//    @DeleteMapping("/{id}")
//    @PreAuthorize("hasAuthority('DELETE_PROGRAM')")
//    public ResponseEntity<Void> deleteProgram(@PathVariable Long id) {
//        try {
//            programService.deleteProgram(id);
//            return ResponseEntity.ok().build();
//        } catch (EntityNotFoundException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    @GetMapping("/by-name/{name}")
//    @PreAuthorize("hasAuthority('VIEW_PROGRAM')")
//    public ResponseEntity<ProgramDTO> getProgramByName(@PathVariable String name) {
//        try {
//            ProgramDTO program = programService.getProgramByName(name);
//            return ResponseEntity.ok(program);
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//        }
//    }
//
//    @GetMapping("/level")
//    @PreAuthorize("hasAuthority('VIEW_PROGRAM')")
//    public ResponseEntity<List<LevelDTO>> getLevels() {
//        try {
//            List<LevelDTO> levels = programService.getLevels();
//            return ResponseEntity.ok(levels);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//        }
//    }
//
//    @GetMapping("/specialty/{levelId}")
//    @PreAuthorize("hasAuthority('VIEW_PROGRAM')")
//    public ResponseEntity<List<SpecialtyDTO>> getSpecialtiesByLevel(@PathVariable Long levelId) {
//        try {
//            List<SpecialtyDTO> specialties = programService.getSpecialtiesByLevel(levelId);
//            return ResponseEntity.ok(specialties);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//        }
//    }
//
//    @GetMapping("/subject")
//    @PreAuthorize("hasAuthority('VIEW_PROGRAM')")
//    public ResponseEntity<List<SubjectDTO>> getSubjects() {
//        try {
//            List<SubjectDTO> subjects = programService.getSubjects();
//            return ResponseEntity.ok(subjects);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//        }
//    }
//}


package tn.esprit.new_timetableservice.controllers;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tn.esprit.new_timetableservice.dto.*;
import tn.esprit.new_timetableservice.services.ProgramService;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/program")
@Validated
public class ProgramController {
    private static final Logger log = LoggerFactory.getLogger(ProgramController.class);

    @Autowired
    private ProgramService programService;

    @PostMapping
    @PreAuthorize("hasAuthority('ADD_PROGRAM')")
    public ResponseEntity<?> createProgram(@Valid @RequestBody ProgramDTO request) {
        log.trace("Entering createProgram with request: {}", request);
        try {
            ProgramDTO program = programService.createProgram(request);
            log.debug("Created program: {}", program);
            return ResponseEntity.ok(program);
        } catch (IllegalArgumentException e) {
            log.error("Invalid input: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid input: " + e.getMessage()));
        } catch (DataIntegrityViolationException e) {
            log.error("Database error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("Database error: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Internal server error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EDIT_PROGRAM')")
    public ResponseEntity<?> updateProgram(@PathVariable Long id, @Valid @RequestBody ProgramDTO request) {
        log.trace("Entering updateProgram with id: {}, request: {}", id, request);
        try {
            request.setId(id);
            ProgramDTO program = programService.updateProgram(request);
            log.debug("Updated program: {}", program);
            return ResponseEntity.ok(program);
        } catch (IllegalArgumentException e) {
            log.error("Invalid input: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid input: " + e.getMessage()));
        } catch (EntityNotFoundException e) {
            log.error("Program not found: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Program not found: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Internal server error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error: " + e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_PROGRAM')")
    public ResponseEntity<?> getPrograms() {
        log.trace("Entering getPrograms");
        try {
            List<ProgramDTO> programs = programService.getPrograms();
            log.debug("Retrieved {} programs", programs.size());
            return ResponseEntity.ok(programs);
        } catch (Exception e) {
            log.error("Internal server error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_PROGRAM')")
    public ResponseEntity<?> getProgramById(@PathVariable Long id) {
        log.trace("Entering getProgramById with id: {}", id);
        try {
            ProgramDTO program = programService.getProgramById(id);
            log.debug("Retrieved program: {}", program);
            return ResponseEntity.ok(program);
        } catch (EntityNotFoundException e) {
            log.error("Program not found: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Program not found: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Internal server error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_PROGRAM')")
    public ResponseEntity<?> deleteProgram(@PathVariable Long id) {
        log.trace("Entering deleteProgram with id: {}", id);
        try {
            programService.deleteProgram(id);
            log.debug("Deleted program with id: {}", id);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            log.error("Program not found: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Program not found: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Internal server error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error: " + e.getMessage()));
        }
    }

    @GetMapping("/by-name/{name}")
    @PreAuthorize("hasAuthority('VIEW_PROGRAM')")
    public ResponseEntity<?> getProgramByName(@PathVariable String name) {
        log.trace("Entering getProgramByName with name: {}", name);
        try {
            ProgramDTO program = programService.getProgramByName(name);
            log.debug("Retrieved program: {}", program);
            return ResponseEntity.ok(program);
        } catch (IllegalArgumentException e) {
            log.error("Program not found: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Program not found: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Internal server error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error: " + e.getMessage()));
        }
    }

    @GetMapping("/level")
    @PreAuthorize("hasAuthority('VIEW_PROGRAM')")
    public ResponseEntity<?> getLevels() {
        log.trace("Entering getLevels");
        try {
            List<LevelDTO> levels = programService.getLevels();
            log.debug("Retrieved {} levels", levels.size());
            return ResponseEntity.ok(levels);
        } catch (Exception e) {
            log.error("Internal server error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error: " + e.getMessage()));
        }
    }

    @GetMapping("/specialty/{levelId}")
    @PreAuthorize("hasAuthority('VIEW_PROGRAM')")
    public ResponseEntity<?> getSpecialtiesByLevel(@PathVariable Long levelId) {
        log.trace("Entering getSpecialtiesByLevel with levelId: {}", levelId);
        try {
            List<SpecialtyDTO> specialties = programService.getSpecialtiesByLevel(levelId);
            log.debug("Retrieved {} specialties", specialties.size());
            return ResponseEntity.ok(specialties);
        } catch (Exception e) {
            log.error("Internal server error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error: " + e.getMessage()));
        }
    }

    @GetMapping("/subject")
    @PreAuthorize("hasAuthority('VIEW_PROGRAM')")
    public ResponseEntity<?> getSubjects() {
        log.trace("Entering getSubjects");
        try {
            List<SubjectDTO> subjects = programService.getSubjects();
            log.debug("Retrieved {} subjects", subjects.size());
            return ResponseEntity.ok(subjects);
        } catch (Exception e) {
            log.error("Internal server error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error: " + e.getMessage()));
        }
    }

    @GetMapping("/debug")
    public ResponseEntity<String> debug() {
        log.trace("Entering debug endpoint");
        log.debug("Debug endpoint called");
        return ResponseEntity.ok("Debug endpoint reached");
    }

    public static class ErrorResponse {
        private final String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
