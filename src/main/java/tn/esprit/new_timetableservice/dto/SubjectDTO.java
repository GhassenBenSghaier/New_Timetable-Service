package tn.esprit.new_timetableservice.dto;

import lombok.Data;

@Data
public class SubjectDTO {
    private Long id;
    private String name;
    private Integer defaultHoursPerWeek;
    private String roomType;
}