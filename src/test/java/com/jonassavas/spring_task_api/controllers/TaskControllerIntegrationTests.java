package com.jonassavas.spring_task_api.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonassavas.spring_task_api.TestDataUtil;
import com.jonassavas.spring_task_api.domain.dto.TaskDto;
import com.jonassavas.spring_task_api.domain.entities.TaskEntity;
import com.jonassavas.spring_task_api.domain.entities.TaskGroupEntity;
import com.jonassavas.spring_task_api.services.TaskGroupService;
import com.jonassavas.spring_task_api.services.TaskService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class TaskControllerIntegrationTests {
    
    private TaskService taskService;
    private TaskGroupService taskGroupService;
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Autowired
    public TaskControllerIntegrationTests(TaskService taskService, 
                                          TaskGroupService taskGroupService, 
                                          MockMvc mockMvc, 
                                          ObjectMapper objectMapper){
        this.taskService = taskService;
        this.taskGroupService = taskGroupService;
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper; 
    } 

    @Test
    public void testThatCreateTaskReturnsHttp201Create() throws Exception{
        TaskGroupEntity testTaskGroupEntityA = TestDataUtil.createTaskGroupEntityA();
        taskGroupService.save(testTaskGroupEntityA);

        TaskDto testTaskDtoA = TestDataUtil.createTestTaskDtoA();

        testTaskDtoA.setTaskGroupId(testTaskGroupEntityA.getId());

        String taskJson = objectMapper.writeValueAsString(testTaskDtoA);

        mockMvc.perform(
            MockMvcRequestBuilders.post("/taskgroups/" + testTaskGroupEntityA.getId() + "/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(taskJson)
        ).andExpect(
            MockMvcResultMatchers.status().isCreated()
        );
    }

    @Test
    public void testThatCreateTaskReturnsSavedTask() throws Exception{
        TaskGroupEntity testTaskGroupEntityA = TestDataUtil.createTaskGroupEntityA();
        taskGroupService.save(testTaskGroupEntityA);

        TaskDto testTaskDtoA = TestDataUtil.createTestTaskDtoA();

        testTaskDtoA.setTaskGroupId(testTaskGroupEntityA.getId());

        String taskJson = objectMapper.writeValueAsString(testTaskDtoA);

        mockMvc.perform(
            MockMvcRequestBuilders.post("/taskgroups/" + testTaskGroupEntityA.getId() + "/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(taskJson)
        ).andExpect(
            MockMvcResultMatchers.jsonPath("$.taskGroupId").isNumber()
        ).andExpect(
            MockMvcResultMatchers.jsonPath("$.id").isNumber()
        ).andExpect( 
            MockMvcResultMatchers.jsonPath("$.taskName").value("Task A")
        );
    }
    
    // TODO
    @Test
    public void testThatCreateTaskWithoutValidTaskGroupReturns404() throws Exception{
        // TaskGroupEntity testTaskGroupEntityA = TestDataUtil.createTaskGroupEntityA();
        // taskGroupService.save(testTaskGroupEntityA);

        // TaskDto testTaskDtoA = TestDataUtil.createTestTaskDtoA();

        // testTaskDtoA.setTaskGroupId(testTaskGroupEntityA.getId());

        // String taskJson = objectMapper.writeValueAsString(testTaskDtoA);

        // mockMvc.perform(
        //     MockMvcRequestBuilders.post("/taskgroups/" + testTaskGroupEntityA.getId() + "/tasks")
        //     .contentType(MediaType.APPLICATION_JSON)
        //     .content(taskJson)
        // ).andExpect(
        //     MockMvcResultMatchers.jsonPath("$.taskGroupId").isNumber()
        // ).andExpect(
        //     MockMvcResultMatchers.jsonPath("$.id").isNumber()
        // ).andExpect( 
        //     MockMvcResultMatchers.jsonPath("$.taskName").value("Task A")
        // );
    }

    @Test
    public void testThatDeleteTaskReturnsHttp204() throws Exception{
        TaskGroupEntity testTaskGroupEntityA = TestDataUtil.createTaskGroupEntityA();
        taskGroupService.save(testTaskGroupEntityA);

        TaskEntity testTaskEntityA = TestDataUtil.createTestTaskEntityA();
        taskService.createTask(testTaskGroupEntityA.getId(), testTaskEntityA);

        assertThat(testTaskGroupEntityA.getTasks()).hasSize(1);

        mockMvc.perform(
            MockMvcRequestBuilders.delete("/tasks/" + testTaskEntityA.getId())
            .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(
            MockMvcResultMatchers.status().isNoContent());

        assertThat(testTaskGroupEntityA.getTasks()).isEmpty();
    }


}
