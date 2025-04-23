package tn.esprit.new_timetableservice.controllers;

import org.springframework.http.ResponseEntity;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tn.esprit.new_timetableservice.dto.SchoolDTO;
import tn.esprit.new_timetableservice.entities.School;
import tn.esprit.new_timetableservice.services.SchoolService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/schools")
@Validated
public class SchoolController {
    private final SchoolService service;

    public SchoolController(SchoolService service) {
        this.service = service;
    }

    @PostMapping

    public ResponseEntity<SchoolDTO> create(@Valid @RequestBody SchoolDTO schoolDTO) {
        School school = new School();
        school.setName(schoolDTO.getName());
        school.setAcademicYear(schoolDTO.getAcademicYear());
        school.setRegion(schoolDTO.getRegion());
        school.setType(schoolDTO.getType());
        School savedSchool = service.create(school);
        return ResponseEntity.ok(toDto(savedSchool));
    }

    @PutMapping("/{id}")

    public ResponseEntity<SchoolDTO> update(@PathVariable Long id, @Valid @RequestBody SchoolDTO schoolDTO) {
        School school = new School();
        school.setId(id);
        school.setName(schoolDTO.getName());
        school.setAcademicYear(schoolDTO.getAcademicYear());
        school.setRegion(schoolDTO.getRegion());
        school.setType(schoolDTO.getType());
        School updatedSchool = service.update(id, school);
        return ResponseEntity.ok(toDto(updatedSchool));
    }

    @GetMapping("/{id}")

    public ResponseEntity<SchoolDTO> findById(@PathVariable Long id) {
        School school = service.findById(id);
        return ResponseEntity.ok(toDto(school));
    }

    @GetMapping

    public ResponseEntity<List<SchoolDTO>> findAll() {
        List<SchoolDTO> schoolDTOs = service.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(schoolDTOs);
    }

    @DeleteMapping("/{id}")

    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private SchoolDTO toDto(School school) {
        SchoolDTO dto = new SchoolDTO();
        dto.setId(school.getId());
        dto.setName(school.getName());
        dto.setAcademicYear(school.getAcademicYear());
        dto.setRegion(school.getRegion());
        dto.setType(school.getType());
        return dto;
    }

    private School toEntity(SchoolDTO dto) {
        School school = new School();
        school.setId(dto.getId());
        school.setName(dto.getName());
        school.setAcademicYear(dto.getAcademicYear());
        school.setRegion(dto.getRegion());
        school.setType(dto.getType());
        return school;
    }
}
