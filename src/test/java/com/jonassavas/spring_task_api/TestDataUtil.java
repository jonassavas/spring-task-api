package com.jonassavas.spring_task_api;

import com.jonassavas.spring_task_api.domain.dto.TaskRequestDto;
import com.jonassavas.spring_task_api.domain.dto.TaskGroupRequestDto;
import com.jonassavas.spring_task_api.domain.dto.TaskDto;
import com.jonassavas.spring_task_api.domain.entities.TaskBoardEntity;
import com.jonassavas.spring_task_api.domain.entities.TaskEntity;
import com.jonassavas.spring_task_api.domain.entities.TaskGroupEntity;

public class TestDataUtil {


    public static TaskBoardEntity createTestTaskBoardEntityA(){
        // This will require a user at some point
        return TaskBoardEntity.builder()
                                .name(null)
                                .build();
    }

    public static TaskRequestDto createTestCreateTaskDto(TaskGroupEntity taskGroup){
        return TaskRequestDto.builder()
                            .taskName("Create Task Dto")
                            .taskGroupId(taskGroup.getId())
                            .build();
    }

    public static TaskDto createTestTaskDtoA(TaskGroupEntity taskGroup){
        return TaskDto.builder()
                        .taskGroupId(taskGroup.getId())
                        .taskName("Task A")
                        .build();
    }

    public static TaskEntity createTestTaskEntityA(TaskGroupEntity taskGroup){
        return TaskEntity.builder()
                            .taskName("HomeworkA")
                            .taskGroup(taskGroup)
                            .build();
    }

    public static TaskEntity createTestTaskEntityB(TaskGroupEntity taskGroup){
        return TaskEntity.builder()
                            .taskName("HomeworkB")
                            .taskGroup(taskGroup)
                            .build();
    }

    public static TaskEntity createTestTaskEntityC(TaskGroupEntity taskGroup){
        return TaskEntity.builder()
                            .taskName("HomeworkC")
                            .taskGroup(taskGroup)
                            .build();
    }
    
    public static TaskGroupEntity createTaskGroupEntityA(TaskBoardEntity taskBoard){
        return TaskGroupEntity.builder()
                                .taskBoard(taskBoard)
                                .taskGroupName("Task Group A")
                                .build();
    }

    public static TaskGroupEntity createTaskGroupEntityB(TaskBoardEntity taskBoard){
        return TaskGroupEntity.builder()
                                .taskBoard(taskBoard)
                                .taskGroupName("Task Group B")
                                .build();
    }

    public static TaskGroupEntity createTaskGroupEntityC(TaskBoardEntity taskBoard){
        return TaskGroupEntity.builder()
                                .taskBoard(taskBoard)
                                .taskGroupName("Task Group C")
                                .build();
    }

    public static TaskGroupRequestDto createTaskGroupDtoA(TaskBoardEntity taskBoard){
        return TaskGroupRequestDto.builder()
                                .taskBoardId(taskBoard.getId())
                                .taskGroupName("Task Group A")
                                .build();
    }

    public static TaskGroupRequestDto createTaskGroupDtoB(TaskBoardEntity taskBoard){
        return TaskGroupRequestDto.builder()
                                .taskBoardId(taskBoard.getId())
                                .taskGroupName("Task Group B")
                                .build();
    }

    public static TaskGroupRequestDto createTaskGroupDtoC(TaskBoardEntity taskBoard){
        return TaskGroupRequestDto.builder()
                                .taskBoardId(taskBoard.getId())
                                .taskGroupName("Task Group C")
                                .build();
    }
}
