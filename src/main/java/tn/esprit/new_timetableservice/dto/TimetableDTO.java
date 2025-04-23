package tn.esprit.new_timetableservice.dto;

import java.util.List;

public class TimetableDTO {
    private Long id;
    private Long schoolId;
    private String status;
    private String generatedAt;
    private List<ScheduleDTO> schedules;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSchoolId() { return schoolId; }
    public void setSchoolId(Long schoolId) { this.schoolId = schoolId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(String generatedAt) { this.generatedAt = generatedAt; }
    public List<ScheduleDTO> getSchedules() { return schedules; }
    public void setSchedules(List<ScheduleDTO> schedules) { this.schedules = schedules; }
}