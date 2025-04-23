package tn.esprit.new_timetableservice.dto;

import java.util.Map;

public class GenerateTimetableRequestDTO {
    private Long schoolId;
    private Map<Long, Integer> programClassCounts;

    public Long getSchoolId() { return schoolId; }
    public void setSchoolId(Long schoolId) { this.schoolId = schoolId; }
    public Map<Long, Integer> getProgramClassCounts() { return programClassCounts; }
    public void setProgramClassCounts(Map<Long, Integer> programClassCounts) { this.programClassCounts = programClassCounts; }
}