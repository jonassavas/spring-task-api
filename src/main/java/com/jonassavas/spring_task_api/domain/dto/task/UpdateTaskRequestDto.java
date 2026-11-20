package com.jonassavas.spring_task_api.domain.dto.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateTaskRequestDto {

    @Size(min = 1, max = 100, message = "Task name must be between 1-100 characters")
    private String taskName;

    @Positive(message = "Task group ID must be positive")
    @Schema(description = "Move the task to this owned group; it is appended there")
    private Long taskGroupId;

    @Schema(
            description = "ISO date; send null to clear it",
            example = "2026-12-01",
            nullable = true)
    private LocalDate dueDate;

    @JsonIgnore private boolean dueDateSupplied;

    @JsonSetter("dueDate")
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
        this.dueDateSupplied = true;
    }
}
