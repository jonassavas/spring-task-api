package com.jonassavas.util;

import com.jonassavas.spring_task_api.domain.dto.task_board.CreateTaskBoardRequestDto;
import com.jonassavas.spring_task_api.domain.dto.task_board.UpdateTaskBoardRequestDto;
import com.jonassavas.spring_task_api.domain.entities.TaskBoardEntity;
import com.jonassavas.spring_task_api.domain.entities.UserEntity;

public class TestTaskBoardData {

    // Entities ----------------------------------------------------------

    public static TaskBoardEntity createTestTaskBoardEntityA(UserEntity user) {
        return TaskBoardEntity.builder()
                .taskBoardName("Task Board A")
                .position(0)
                .owner(user)
                .build();
    }

    public static TaskBoardEntity createTestTaskBoardEntityB(UserEntity user) {
        return TaskBoardEntity.builder()
                .taskBoardName("Task Board B")
                .position(1)
                .owner(user)
                .build();
    }

    public static TaskBoardEntity createTestTaskBoardEntityC(UserEntity user) {
        return TaskBoardEntity.builder()
                .taskBoardName("Task Board C")
                .position(2)
                .owner(user)
                .build();
    }

    // CREATE DTOs -------------------------------------------------------------
    public static CreateTaskBoardRequestDto createTestTaskBoardRequestDtoA() {
        // This will require a user at some point
        return CreateTaskBoardRequestDto.builder().taskBoardName("Task Board A").build();
    }

    public static CreateTaskBoardRequestDto createTestTaskBoardRequestDtoB() {
        // This will require a user at some point
        return CreateTaskBoardRequestDto.builder().taskBoardName("Task Board B").build();
    }

    public static CreateTaskBoardRequestDto createTestTaskBoardRequestDtoC() {
        // This will require a user at some point
        return CreateTaskBoardRequestDto.builder().taskBoardName("Task Board C").build();
    }

    // UPDATE DTOs -------------------------------------------------------------
    public static UpdateTaskBoardRequestDto updateTestTaskBoardRequestDtoA() {
        // This will require a user at some point
        return UpdateTaskBoardRequestDto.builder().taskBoardName("Task Board A").build();
    }

    public static UpdateTaskBoardRequestDto updateTestTaskBoardRequestDtoB() {
        // This will require a user at some point
        return UpdateTaskBoardRequestDto.builder().taskBoardName("Task Board B").build();
    }

    public static UpdateTaskBoardRequestDto updateTestTaskBoardRequestDtoC() {
        // This will require a user at some point
        return UpdateTaskBoardRequestDto.builder().taskBoardName("Task Board C").build();
    }
}
