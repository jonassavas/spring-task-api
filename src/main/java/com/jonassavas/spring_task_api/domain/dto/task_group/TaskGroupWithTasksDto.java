package com.jonassavas.spring_task_api.domain.dto.task_group;

import com.jonassavas.spring_task_api.domain.dto.task.TaskDto;
import com.jonassavas.spring_task_api.domain.entities.TaskSortMode;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskGroupWithTasksDto {
    private Long id;

    private String taskGroupName;

    private String color;

    private Integer position;

    private boolean strikeThroughTasks;

    private TaskSortMode sortMode;

    private List<TaskDto> tasks;

    private Long taskBoardId;
}
