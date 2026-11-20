package com.jonassavas.spring_task_api.domain.dto.task;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskDto {
    private Long id;

    private Long taskGroupId;

    private String taskName;

    private Integer position;

    private LocalDate dueDate;
}
