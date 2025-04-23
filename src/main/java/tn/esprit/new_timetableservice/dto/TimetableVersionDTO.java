// src/main/java/tn/esprit/new_timetableservice/dto/TimetableVersionDTO.java
package tn.esprit.new_timetableservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class TimetableVersionDTO {
    private Long id;
    private SchoolDTO school;
    private Integer versionNumber;
    private String status;
    private String generatedAt;
    private String createdBy;
    private List<ScheduleDTO> schedules;
}