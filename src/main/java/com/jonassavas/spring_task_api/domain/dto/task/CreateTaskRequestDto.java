package com.jonassavas.spring_task_api.domain.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
public class CreateTaskRequestDto {
    @NotBlank(message = "Task name is required")
    @Size(max = 100, message = "Task name must be between 1-100 characters")
    private String taskName;

    @Schema(description = "Optional ISO date used by date-sorted groups", example = "2026-12-01")
    private LocalDate dueDate;
}
