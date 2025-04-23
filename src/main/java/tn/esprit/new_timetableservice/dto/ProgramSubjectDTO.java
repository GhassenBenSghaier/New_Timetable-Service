package tn.esprit.new_timetableservice.dto;

import lombok.Data;

@Data
public class ProgramSubjectDTO {
    private Long id;
    private Long subjectId;
    private Integer hoursPerWeek;
    private Boolean isCore;
}