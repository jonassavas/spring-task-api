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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonassavas.spring_task_api.TestDataUtil;
import com.jonassavas.spring_task_api.domain.dto.CreateTaskDto;
import com.jonassavas.spring_task_api.domain.dto.CreateTaskGroupDto;
import com.jonassavas.spring_task_api.domain.entities.TaskEntity;
import com.jonassavas.spring_task_api.domain.entities.TaskGroupEntity;
import com.jonassavas.spring_task_api.services.TaskGroupService;
import com.jonassavas.spring_task_api.services.TaskService;

import jakarta.transaction.Transactional;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class TaskGroupControllerIntegrationTests {
   
    private TaskGroupService taskGroupService;
    private TaskService taskService;
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Autowired
    public TaskGroupControllerIntegrationTests(MockMvc mockMvc,
                                            TaskGroupService taskGroupService, 
                                            TaskService taskService,
                                            ObjectMapper objectMapper){
        this.mockMvc = mockMvc;
        this.taskGroupService = taskGroupService;
        this.taskService = taskService;
        this.objectMapper = objectMapper;
    }

    @Test
    public void testThatCreateTaskGroupReturnsHttp201Create() throws Exception{
        TaskGroupEntity testTaskGroupEntityA = TestDataUtil.createTaskGroupEntityA();
        testTaskGroupEntityA.setId(null);
        String taskGroupJson = objectMapper.writeValueAsString(testTaskGroupEntityA);

        mockMvc.perform(
            MockMvcRequestBuilders.post("/taskgroups")
            .contentType(MediaType.APPLICATION_JSON)
            .content(taskGroupJson)
        ).andExpect(
            MockMvcResultMatchers.status().isCreated()
        );
    }

    @Test
    public void testThatCreateTaskGroupReturnsSavedTaskGroup() throws Exception{
        TaskGroupEntity testTaskGroupEntityA = TestDataUtil.createTaskGroupEntityA();
        testTaskGroupEntityA.setId(null);
        String taskGroupJson = objectMapper.writeValueAsString(testTaskGroupEntityA);

        mockMvc.perform(
            MockMvcRequestBuilders.post("/taskgroups")
            .contentType(MediaType.APPLICATION_JSON)
            .content(taskGroupJson)
        ).andExpect(
            MockMvcResultMatchers.jsonPath("$.id").isNumber()
        ).andExpect(
            MockMvcResultMatchers.jsonPath("$.taskGroupName").value("Task Group A")
        );
    }

    @Test
    public void testThatListTaskGroupsReturnsHttpStatus200() throws Exception{
        mockMvc.perform(
            MockMvcRequestBuilders.get("/taskgroups")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(
            MockMvcResultMatchers.status().isOk()
        );
    }

    @Test
    public void testThatListTaskGroupsReturnsListOfTaskGroups() throws Exception{
        TaskGroupEntity testTaskGroupEntityA = TestDataUtil.createTaskGroupEntityA();
        taskGroupService.save(testTaskGroupEntityA);
        
        mockMvc.perform(
            MockMvcRequestBuilders.get("/taskgroups")
                .contentType(MediaType.APPLICATION_JSON)    
        ).andExpect(
            MockMvcResultMatchers.jsonPath("$[0].id").isNumber()
        ).andExpect(
            MockMvcResultMatchers.jsonPath("$[0].taskGroupName").value("Task Group A")
        );
    }


    @Test
    public void testThatDeleteTaskGroupReturnsHttp204() throws Exception{
        TaskGroupEntity testTaskGroupEntityA = TestDataUtil.createTaskGroupEntityA();
        taskGroupService.save(testTaskGroupEntityA);

        assertThat(taskGroupService.findAll().size()).isEqualTo(1); 

        mockMvc.perform(
            MockMvcRequestBuilders.delete("/taskgroups/" + testTaskGroupEntityA.getId())
                .contentType(MediaType.APPLICATION_JSON)    
        ).andExpect(
            MockMvcResultMatchers.status().isNoContent()
        );

        assertThat(taskGroupService.findAll().size()).isEqualTo(0); 
    }
    

    @Transactional
    @Test
    public void testThatDeleteTaskGroupDeletesCorrectGroup() throws Exception{
        TaskGroupEntity testTaskGroupEntityA = TestDataUtil.createTaskGroupEntityA();
        taskGroupService.save(testTaskGroupEntityA);
        TaskGroupEntity testTaskGroupEntityB = TestDataUtil.createTaskGroupEntityB();
        taskGroupService.save(testTaskGroupEntityB);
        

        assertThat(taskGroupService.findAll().size()).isEqualTo(2); 

        mockMvc.perform(
            MockMvcRequestBuilders.delete("/taskgroups/" + testTaskGroupEntityA.getId())
                .contentType(MediaType.APPLICATION_JSON)    
        ).andExpect(
            MockMvcResultMatchers.status().isNoContent()
        );

        List<TaskGroupEntity> result = taskGroupService.findAll();
        assertThat(result.size()).isEqualTo(1); 
        assertThat(result)
                .extracting(TaskGroupEntity::getId)
                .containsExactly(testTaskGroupEntityB.getId());
    }

    @Transactional
    @Test
    public void testThatDeleteTaskGroupDeletesTasks() throws Exception{
        TaskGroupEntity testTaskGroupEntityA = TestDataUtil.createTaskGroupEntityA();
        taskGroupService.save(testTaskGroupEntityA);

        TaskEntity testTaskEntityA = TestDataUtil.createTestTaskEntityA();
        taskService.createTask(testTaskGroupEntityA.getId(), testTaskEntityA);
        TaskEntity testTaskEntityB = TestDataUtil.createTestTaskEntityB();
        taskService.createTask(testTaskGroupEntityA.getId(), testTaskEntityB);
        TaskEntity testTaskEntityC = TestDataUtil.createTestTaskEntityC();
        taskService.createTask(testTaskGroupEntityA.getId(), testTaskEntityC);

        List<TaskGroupEntity> result = taskGroupService.findAll();
        assertThat(result.size()).isEqualTo(1); 
        assertThat(result.getFirst().getTasks())
                                    .extracting(TaskEntity::getId)
                                    .containsExactly(testTaskEntityA.getId(), 
                                                        testTaskEntityB.getId(), 
                                                        testTaskEntityC.getId());
        assertThat(taskService.findAll())
                                    .extracting(TaskEntity::getId)
                                    .containsExactly(testTaskEntityA.getId(), 
                                                        testTaskEntityB.getId(), 
                                                        testTaskEntityC.getId());

        mockMvc.perform(
            MockMvcRequestBuilders.delete("/taskgroups/" + testTaskGroupEntityA.getId())
                .contentType(MediaType.APPLICATION_JSON)    
        ).andExpect(
            MockMvcResultMatchers.status().isNoContent()
        );
        
        assertThat(taskGroupService.findAll().size()).isEqualTo(0);
        assertThat(taskService.findAll().size()).isEqualTo(0);
    }


    @Transactional
    @Test
    public void testThatDeleteTaskGroupOnlyDeletesOwnTasks() throws Exception{
        TaskGroupEntity testTaskGroupEntityA = TestDataUtil.createTaskGroupEntityA();
        taskGroupService.save(testTaskGroupEntityA);

        TaskEntity testTaskEntityA = TestDataUtil.createTestTaskEntityA();
        taskService.createTask(testTaskGroupEntityA.getId(), testTaskEntityA);
        
        TaskGroupEntity testTaskGroupEntityB = TestDataUtil.createTaskGroupEntityB();
        taskGroupService.save(testTaskGroupEntityB);
       
        TaskEntity testTaskEntityB = TestDataUtil.createTestTaskEntityB();
        taskService.createTask(testTaskGroupEntityB.getId(), testTaskEntityB);
        TaskEntity testTaskEntityC = TestDataUtil.createTestTaskEntityC();
        taskService.createTask(testTaskGroupEntityB.getId(), testTaskEntityC);

        List<TaskGroupEntity> result = taskGroupService.findAll();
        assertThat(result.size()).isEqualTo(2); 
        assertThat(result.get(0).getTasks())
                                    .extracting(TaskEntity::getId)
                                    .containsExactly(testTaskEntityA.getId());
        assertThat(result.get(1).getTasks())
                                    .extracting(TaskEntity::getId)
                                    .containsExactly(testTaskEntityB.getId(),
                                                     testTaskEntityC.getId());
        assertThat(taskService.findAll())
                                    .extracting(TaskEntity::getId)
                                    .containsExactly(testTaskEntityA.getId(), 
                                                        testTaskEntityB.getId(), 
                                                        testTaskEntityC.getId());

        mockMvc.perform(
            MockMvcRequestBuilders.delete("/taskgroups/" + testTaskGroupEntityA.getId())
                .contentType(MediaType.APPLICATION_JSON)    
        ).andExpect(
            MockMvcResultMatchers.status().isNoContent()
        );

        result = taskGroupService.findAll();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result)
                .extracting(TaskGroupEntity::getId)
                .containsExactly(testTaskGroupEntityB.getId());
        assertThat(result.get(0).getTasks())
                                    .extracting(TaskEntity::getId)
                                    .containsExactly(testTaskEntityB.getId(),
                                                     testTaskEntityC.getId());
    }


    @Test
    public void testThatUpdateTaskGroupReturnsHttp200() throws Exception{
        TaskGroupEntity testTaskGroupEntityA = TestDataUtil.createTaskGroupEntityA();
        taskGroupService.save(testTaskGroupEntityA);

        CreateTaskGroupDto testTaskGroupDtoA = TestDataUtil.createTaskGroupDtoA();
        testTaskGroupDtoA.setTaskGroupName("UPDATED");
        String taskGroupJson = objectMapper.writeValueAsString(testTaskGroupDtoA);

        mockMvc.perform(
            MockMvcRequestBuilders.patch("/taskgroups/" + testTaskGroupEntityA.getId())
                .contentType(MediaType.APPLICATION_JSON)   
                .content(taskGroupJson) 
        ).andExpect(
            MockMvcResultMatchers.status().isOk()
        ).andExpect(
            MockMvcResultMatchers.jsonPath("$.taskGroupName").value("UPDATED")
        );
    }

    // TODO 
    // - Update task group name etc
    // - DELETE all tasks
    // - 
}
