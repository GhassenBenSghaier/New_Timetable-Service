package tn.esprit.new_timetableservice.dto;

import java.util.Map;

public class CapacityResponse {
    private Long schoolId;
    private boolean feasible;
    private Map<Long, Integer> maxClasses;
    private Map<Long, ConstraintCheck> teacherConstraints;
    private Map<String, ConstraintCheck> roomConstraints;
    private ConstraintCheck slotConstraint;
    private ConstraintCheck classCountConstraint;
    private String message;

    public CapacityResponse(
            Long schoolId,
            boolean feasible,
            Map<Long, Integer> maxClasses,
            Map<Long, ConstraintCheck> teacherConstraints,
            Map<String, ConstraintCheck> roomConstraints,
            ConstraintCheck slotConstraint,
            ConstraintCheck classCountConstraint,
            String message) {
        this.schoolId = schoolId;
        this.feasible = feasible;
        this.maxClasses = maxClasses;
        this.teacherConstraints = teacherConstraints;
        this.roomConstraints = roomConstraints;
        this.slotConstraint = slotConstraint;
        this.classCountConstraint = classCountConstraint;
        this.message = message;
    }

    // Getters and setters
    public Long getSchoolId() { return schoolId; }
    public void setSchoolId(Long schoolId) { this.schoolId = schoolId; }
    public boolean isFeasible() { return feasible; }
    public void setFeasible(boolean feasible) { this.feasible = feasible; }
    public Map<Long, Integer> getMaxClasses() { return maxClasses; }
    public void setMaxClasses(Map<Long, Integer> maxClasses) { this.maxClasses = maxClasses; }
    public Map<Long, ConstraintCheck> getTeacherConstraints() { return teacherConstraints; }
    public void setTeacherConstraints(Map<Long, ConstraintCheck> teacherConstraints) { this.teacherConstraints = teacherConstraints; }
    public Map<String, ConstraintCheck> getRoomConstraints() { return roomConstraints; }
    public void setRoomConstraints(Map<String, ConstraintCheck> roomConstraints) { this.roomConstraints = roomConstraints; }
    public ConstraintCheck getSlotConstraint() { return slotConstraint; }
    public void setSlotConstraint(ConstraintCheck slotConstraint) { this.slotConstraint = slotConstraint; }
    public ConstraintCheck getClassCountConstraint() { return classCountConstraint; }
    public void setClassCountConstraint(ConstraintCheck classCountConstraint) { this.classCountConstraint = classCountConstraint; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
