package tn.esprit.new_timetableservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


import java.util.List;

@Data
public class ProgramDTO {
    private Long id;
    private Long levelId;
    private Long specialtyId;
    @NotBlank(message = "Program name cannot be blank")
    private String name;
    private List<ProgramSubjectDTO> programSubjects;
}