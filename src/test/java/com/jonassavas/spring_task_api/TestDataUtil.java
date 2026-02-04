package com.jonassavas.spring_task_api;

import com.jonassavas.spring_task_api.domain.dto.CreateTaskDto;
import com.jonassavas.spring_task_api.domain.dto.TaskGroupRequestDto;
import com.jonassavas.spring_task_api.domain.dto.TaskDto;
import com.jonassavas.spring_task_api.domain.entities.TaskEntity;
import com.jonassavas.spring_task_api.domain.entities.TaskGroupEntity;

public class TestDataUtil {

    public static CreateTaskDto createTestCreateTaskDto(){
        return CreateTaskDto.builder()
                            .taskName("Create Task Dto")
                            .build();
    }

    public static TaskDto createTestTaskDtoA(){
        return TaskDto.builder()
                        .taskGroupId(null)
                        .taskName("Task A")
                        .build();
    }

    public static TaskEntity createTestTaskEntityA(){
        return TaskEntity.builder()
                            .taskName("HomeworkA")
                            .taskGroup(null)
                            .build();
    }

    public static TaskEntity createTestTaskEntityB(){
        return TaskEntity.builder()
                            .taskName("HomeworkB")
                            .taskGroup(null)
                            .build();
    }

    public static TaskEntity createTestTaskEntityC(){
        return TaskEntity.builder()
                            .taskName("HomeworkC")
                            .taskGroup(null)
                            .build();
    }
    
    public static TaskGroupEntity createTaskGroupEntityA(){
        return TaskGroupEntity.builder()
                                .taskGroupName("Task Group A")
                                .build();
    }

    public static TaskGroupEntity createTaskGroupEntityB(){
        return TaskGroupEntity.builder()
                                .taskGroupName("Task Group B")
                                .build();
    }

    public static TaskGroupEntity createTaskGroupEntityC(){
        return TaskGroupEntity.builder()
                                .taskGroupName("Task Group C")
                                .build();
    }

    public static TaskGroupRequestDto createTaskGroupDtoA(){
        return TaskGroupRequestDto.builder()
                                .taskGroupName("Task Group A")
                                .build();
    }

    public static TaskGroupRequestDto createTaskGroupDtoB(){
        return TaskGroupRequestDto.builder()
                                .taskGroupName("Task Group B")
                                .build();
    }

    public static TaskGroupRequestDto createTaskGroupDtoC(){
        return TaskGroupRequestDto.builder()
                                .taskGroupName("Task Group C")
                                .build();
    }
}
