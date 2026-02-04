package com.jonassavas.spring_task_api.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

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
import com.jonassavas.spring_task_api.domain.dto.TaskRequestDto;
import com.jonassavas.spring_task_api.domain.dto.TaskDto;
import com.jonassavas.spring_task_api.domain.entities.TaskEntity;
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
    
    @Test
    public void testThatCreateTaskWithoutValidTaskGroupReturns404() throws Exception{
        // TaskGroupEntity testTaskGroupEntityA = TestDataUtil.createTaskGroupEntityA();
        // taskGroupService.save(testTaskGroupEntityA);

        TaskDto testTaskDtoA = TestDataUtil.createTestTaskDtoA();

        testTaskDtoA.setTaskGroupId(99L);

        String taskJson = objectMapper.writeValueAsString(testTaskDtoA);

        mockMvc.perform(
            MockMvcRequestBuilders.post("/taskgroups/" + 99 + "/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(taskJson)
        ).andExpect(
            MockMvcResultMatchers.status().isNotFound()
        );
    }


    @Test
    public void testThatDeleteTaskReturnsHttp204() throws Exception{
        TaskGroupEntity testTaskGroupEntityA = TestDataUtil.createTaskGroupEntityA();
        taskGroupService.save(testTaskGroupEntityA);

        TaskEntity testTaskEntityA = TestDataUtil.createTestTaskEntityA();
        taskService.createTask(testTaskGroupEntityA.getId(), testTaskEntityA);

        assertThat(taskGroupService
                    .findByIdWithTasks(testTaskGroupEntityA.getId())
                        .getTasks().size())
                .isEqualTo(1); 

        mockMvc.perform(
            MockMvcRequestBuilders.delete("/tasks/" + testTaskEntityA.getId())
            .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(
            MockMvcResultMatchers.status().isNoContent());

        assertThat(taskGroupService
                    .findByIdWithTasks(testTaskGroupEntityA.getId())
                        .getTasks().size())
                .isEqualTo(0); 
    }

    @Test
    public void testThatDeleteTaskDeletesCorrectTask() throws Exception{
        TaskGroupEntity testTaskGroupEntityA = TestDataUtil.createTaskGroupEntityA();
        taskGroupService.save(testTaskGroupEntityA);

        TaskEntity testTaskEntityA = TestDataUtil.createTestTaskEntityA();
        taskService.createTask(testTaskGroupEntityA.getId(), testTaskEntityA);

        TaskEntity testTaskEntityB = TestDataUtil.createTestTaskEntityB();
        taskService.createTask(testTaskGroupEntityA.getId(), testTaskEntityB);

        assertThat(taskGroupService
                    .findByIdWithTasks(testTaskGroupEntityA.getId())
                        .getTasks().size())
                .isEqualTo(2); 

        mockMvc.perform(
            MockMvcRequestBuilders.delete("/tasks/" + testTaskEntityA.getId())
            .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(
            MockMvcResultMatchers.status().isNoContent());
        
        List<TaskEntity> result = taskGroupService
                                    .findByIdWithTasks(testTaskGroupEntityA.getId())
                                        .getTasks();

        assertThat(result.size()).isEqualTo(1);
        assertThat(result)
                .extracting(TaskEntity::getId)
                .containsExactly(testTaskEntityB.getId());
        
    }


    @Test
    public void testUpdateTaskName() throws Exception{
        TaskGroupEntity testTaskGroupEntityA = TestDataUtil.createTaskGroupEntityA();
        taskGroupService.save(testTaskGroupEntityA);

        TaskEntity testTaskEntityA = TestDataUtil.createTestTaskEntityA();
        taskService.createTask(testTaskGroupEntityA.getId(), testTaskEntityA);

        TaskRequestDto testCreateTaskDto = TestDataUtil.createTestCreateTaskDto();
        testCreateTaskDto.setTaskName("UPDATED");

        String taskJson = objectMapper.writeValueAsString(testCreateTaskDto);

        mockMvc.perform(
            MockMvcRequestBuilders.patch("/tasks/" + testTaskEntityA.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(taskJson)
        ).andExpect(
            MockMvcResultMatchers.status().isOk()
        ).andExpect(
            MockMvcResultMatchers.jsonPath("$.taskName").value("UPDATED")
        );
    }

    @Test
    public void testUpdateTaskGroupId() throws Exception{
        TaskGroupEntity testTaskGroupEntityA = TestDataUtil.createTaskGroupEntityA();
        taskGroupService.save(testTaskGroupEntityA);

        TaskEntity testTaskEntityA = TestDataUtil.createTestTaskEntityA();
        taskService.createTask(testTaskGroupEntityA.getId(), testTaskEntityA);

        TaskGroupEntity testTaskGroupEntityB = TestDataUtil.createTaskGroupEntityB();
        taskGroupService.save(testTaskGroupEntityB);

        TaskRequestDto testCreateTaskDto = TestDataUtil.createTestCreateTaskDto();
        testCreateTaskDto.setTaskGroupId(testTaskGroupEntityB.getId());

        String taskJson = objectMapper.writeValueAsString(testCreateTaskDto);

        mockMvc.perform(
            MockMvcRequestBuilders.patch("/tasks/" + testTaskEntityA.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(taskJson)
        ).andExpect(
            MockMvcResultMatchers.status().isOk()
        ).andExpect(
            MockMvcResultMatchers.jsonPath("$.taskGroupId").value(testTaskGroupEntityB.getId())
        );
    }

    @Test
    public void testUpdateBothTaskGroupIdAndTaskName() throws Exception{
        TaskGroupEntity testTaskGroupEntityA = TestDataUtil.createTaskGroupEntityA();
        taskGroupService.save(testTaskGroupEntityA);

        TaskEntity testTaskEntityA = TestDataUtil.createTestTaskEntityA();
        taskService.createTask(testTaskGroupEntityA.getId(), testTaskEntityA);

        TaskGroupEntity testTaskGroupEntityB = TestDataUtil.createTaskGroupEntityB();
        taskGroupService.save(testTaskGroupEntityB);

        TaskRequestDto testCreateTaskDto = TestDataUtil.createTestCreateTaskDto();
        testCreateTaskDto.setTaskGroupId(testTaskGroupEntityB.getId());
        testCreateTaskDto.setTaskName("UPDATED");

        String taskJson = objectMapper.writeValueAsString(testCreateTaskDto);

        mockMvc.perform(
            MockMvcRequestBuilders.patch("/tasks/" + testTaskEntityA.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(taskJson)
        ).andExpect(
            MockMvcResultMatchers.status().isOk()
        ).andExpect(
            MockMvcResultMatchers.jsonPath("$.taskGroupId").value(testTaskGroupEntityB.getId())
        ).andExpect(
            MockMvcResultMatchers.jsonPath("$.taskName").value("UPDATED")
        );
    }
}
