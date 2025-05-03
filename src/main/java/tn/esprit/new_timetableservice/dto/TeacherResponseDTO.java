package tn.esprit.new_timetableservice.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
public class TeacherResponseDTO {
    private Long id;
    private String name;
    private Integer maxHoursPerWeek;
    private Long subjectId;
    private String subjectName;
    private Long schoolId;
    private String schoolName;
    private List<ProgramDTO> programs = new ArrayList<>();
}