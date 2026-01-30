package com.jonassavas.spring_task_api;

import com.jonassavas.spring_task_api.domain.dto.CreateTaskGroupDto;
import com.jonassavas.spring_task_api.domain.dto.TaskDto;
import com.jonassavas.spring_task_api.domain.entities.TaskEntity;
import com.jonassavas.spring_task_api.domain.entities.TaskGroupEntity;

public class TestDataUtil {

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

    public static CreateTaskGroupDto createTaskGroupDtoA(){
        return CreateTaskGroupDto.builder()
                                .taskGroupName("Task Group A")
                                .build();
    }

    public static CreateTaskGroupDto createTaskGroupDtoB(){
        return CreateTaskGroupDto.builder()
                                .taskGroupName("Task Group B")
                                .build();
    }

    public static CreateTaskGroupDto createTaskGroupDtoC(){
        return CreateTaskGroupDto.builder()
                                .taskGroupName("Task Group C")
                                .build();
    }
}
