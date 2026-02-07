package com.jonassavas.spring_task_api.domain.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskBoardWithGroupsDto {
    private Long id;
    private String name;
    private List<TaskGroupDto> groups;
}
