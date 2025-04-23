
package tn.esprit.new_timetableservice.dto;

import lombok.Data;

@Data
public class TimeSlotDTO {
    private Long id;
    private String day;
    private String startTime;
    private String endTime;
}