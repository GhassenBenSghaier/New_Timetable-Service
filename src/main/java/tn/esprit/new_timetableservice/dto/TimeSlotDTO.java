package tn.esprit.new_timetableservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class TimeSlotDTO {
    private Long id;

    @NotNull(message = "School ID cannot be null")
    private Long schoolId;

    @NotBlank(message = "Day cannot be blank")
    private String day;

    @NotBlank(message = "Start time cannot be blank")
    @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Start time must be in HH:MM format")
    private String startTime;

    @NotBlank(message = "End time cannot be blank")
    @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$", message = "End time must be in HH:MM format")
    private String endTime;
}