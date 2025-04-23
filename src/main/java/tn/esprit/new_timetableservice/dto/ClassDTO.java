package tn.esprit.new_timetableservice.dto;

import lombok.Data;

@Data
public class ClassDTO {
    private Long id;
    private Long programId;
    private String name;
    private Integer studentCount;
}