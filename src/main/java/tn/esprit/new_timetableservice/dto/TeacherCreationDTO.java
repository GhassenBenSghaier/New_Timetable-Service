package tn.esprit.new_timetableservice.dto;

import lombok.Data;

@Data
public class TeacherCreationDTO {
    private Long id;
    private String name;
    private Integer maxHoursPerWeek;
    private Long subjectId;
    private Long schoolId;
}