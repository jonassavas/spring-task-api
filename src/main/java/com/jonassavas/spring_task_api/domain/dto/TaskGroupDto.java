package com.jonassavas.spring_task_api.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskGroupDto {
    private Long id;

    private String taskGroupName;

    private Long taskBoardId;

}
