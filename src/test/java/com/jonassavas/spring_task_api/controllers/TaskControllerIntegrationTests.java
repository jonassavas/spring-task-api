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
import com.jonassavas.spring_task_api.domain.entities.TaskGroupEntity;
import com.jonassavas.spring_task_api.services.TaskGroupService;
import com.jonassavas.spring_task_api.services.TaskService;

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
}
