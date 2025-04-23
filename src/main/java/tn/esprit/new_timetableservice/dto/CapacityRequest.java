package tn.esprit.new_timetableservice.dto;

import java.util.Map;

public class CapacityRequest {
    private Long schoolId;
    private Map<Long, Integer> desiredClasses;

    // Getters and setters
    public Long getSchoolId() { return schoolId; }
    public void setSchoolId(Long schoolId) { this.schoolId = schoolId; }
    public Map<Long, Integer> getDesiredClasses() { return desiredClasses; }
    public void setDesiredClasses(Map<Long, Integer> desiredClasses) { this.desiredClasses = desiredClasses; }
}