package tn.esprit.new_timetableservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class TeacherDTO {
    private Long id;
    private String name;
    private Integer maxHoursPerWeek;
    private SubjectDTO subject; // Changed from List<SubjectDTO> to single SubjectDTO
    private List<ProgramDTO> programs; // Added to reflect teacher's programs
}