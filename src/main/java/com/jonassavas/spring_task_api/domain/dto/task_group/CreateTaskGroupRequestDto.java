package com.jonassavas.spring_task_api.domain.dto.task_group;

import com.jonassavas.spring_task_api.domain.entities.TaskSortMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateTaskGroupRequestDto {

    @NotBlank(message = "Task group name is required")
    @Size(max = 50, message = "Task group name cannot exceed 50 characters")
    private String taskGroupName;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "Color must be a valid hex color")
    private String color;

    @Schema(description = "Show task names with strikethrough styling", example = "false")
    private Boolean strikeThroughTasks;

    @Schema(description = "Manual order or automatic ascending due-date order", example = "MANUAL")
    private TaskSortMode sortMode;
}
