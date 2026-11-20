package com.jonassavas.spring_task_api.domain.dto.task_board;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateTaskBoardRequestDto {

    @NotBlank(message = "Task board name is required")
    @Size(min = 1, max = 50, message = "Task board name cannot exceed 50 characters")
    private String taskBoardName;

    @Schema(description = "Create the five default task groups atomically", example = "true")
    private boolean useDefaultConfiguration;
}
