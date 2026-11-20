package com.jonassavas.spring_task_api.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import com.jonassavas.spring_task_api.domain.entities.TaskBoardEntity;
import com.jonassavas.spring_task_api.mappers.impl.task_board.TaskBoardMapper;
import com.jonassavas.spring_task_api.repositories.TaskBoardRepository;
import com.jonassavas.spring_task_api.repositories.TaskGroupRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

class BoardPresetRollbackIntegrationTest extends BaseAuthenticatedIntegrationTest {
    @MockitoSpyBean private TaskBoardMapper boardMapper;
    @Autowired private TaskBoardRepository boardRepository;
    @Autowired private TaskGroupRepository groupRepository;

    @Test
    void rollsBackBoardAndGroupsWhenCreationFails() throws Exception {
        doThrow(new IllegalStateException("simulated mapping failure"))
                .when(boardMapper)
                .mapTo(any(TaskBoardEntity.class));

        mockMvc.perform(
                        authenticated(MockMvcRequestBuilders.post("/taskboards"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"taskBoardName\":\"Rollback\","
                                                + "\"useDefaultConfiguration\":true}"))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());

        assertThat(boardRepository.findByOwnerUsername(user.getUsername())).isEmpty();
        assertThat(groupRepository.findAllWithTasksByUsername(user.getUsername())).isEmpty();
    }
}
