package tn.esprit.new_timetableservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ClassroomDTO {
    private Long id;

    @NotNull(message = "School ID cannot be null")
    private Long schoolId;

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotNull(message = "Capacity cannot be null")
    @Positive(message = "Capacity must be positive")
    private Integer capacity;

    private String type;
}