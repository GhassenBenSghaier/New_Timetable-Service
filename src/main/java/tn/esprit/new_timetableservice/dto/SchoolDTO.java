package tn.esprit.new_timetableservice.dto;
import lombok.Data;

@Data
public class SchoolDTO {
    private Long id;
    private String name;
    private String academicYear;
    private String region;
    private String type;
}