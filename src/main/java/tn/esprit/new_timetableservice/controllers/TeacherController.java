package tn.esprit.new_timetableservice.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.new_timetableservice.dto.TeacherCreationDTO;
import tn.esprit.new_timetableservice.dto.TeacherResponseDTO;
import tn.esprit.new_timetableservice.dto.ProgramDTO;
import tn.esprit.new_timetableservice.services.TeacherService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teachers")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @PostMapping
    @PreAuthorize("hasAuthority('ADD_USER_TEACHER')")
    public ResponseEntity<TeacherResponseDTO> createTeacher(@RequestBody TeacherCreationDTO teacherDTO) {
        TeacherResponseDTO responseDTO = teacherService.createTeacher(teacherDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EDIT_USER_TEACHER')")
    public ResponseEntity<TeacherResponseDTO> updateTeacher(@PathVariable Long id, @RequestBody TeacherCreationDTO teacherDTO) {
        TeacherResponseDTO responseDTO = teacherService.updateTeacher(id, teacherDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_USER_TEACHER')")
    public ResponseEntity<Void> deleteTeacher(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/programs")
    @PreAuthorize("hasAuthority('EDIT_USER_TEACHER')")
    public ResponseEntity<List<ProgramDTO>> getTeacherPrograms(@PathVariable Long id) {
        List<ProgramDTO> programs = teacherService.getTeacherPrograms(id);
        return ResponseEntity.ok(programs);
    }

    @PostMapping("/{id}/programs/{programId}")
    @PreAuthorize("hasAuthority('EDIT_USER_TEACHER')")
    public ResponseEntity<Void> addProgramToTeacher(@PathVariable Long id, @PathVariable Long programId) {
        teacherService.addProgramToTeacher(id, programId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/programs/{programId}")
    @PreAuthorize("hasAuthority('EDIT_USER_TEACHER')")
    public ResponseEntity<Void> removeProgramFromTeacher(@PathVariable Long id, @PathVariable Long programId) {
        teacherService.removeProgramFromTeacher(id, programId);
        return ResponseEntity.ok().build();
    }
}