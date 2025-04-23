package tn.esprit.new_timetableservice.dto;
import lombok.Data;

@Data
public class ScheduleDTO {
    private Long id;
    private ClassDTO classEntity;
    private SubjectDTO subject;
    private TeacherDTO teacher;
    private ClassroomDTO classroom;
    private TimeSlotDTO timeSlot;
}