package com.jonassavas.spring_task_api.repositories;

import org.springframework.boot.test.context.SpringBootTest;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import com.jonassavas.spring_task_api.TestDataUtil;
import com.jonassavas.spring_task_api.domain.entities.TaskBoardEntity;
import com.jonassavas.spring_task_api.domain.entities.TaskEntity;
import com.jonassavas.spring_task_api.domain.entities.TaskGroupEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TaskGroupEntityRepositoryIntegrationTests {
    
    TaskGroupRepository underTest;
    private TaskBoardRepository taskBoardRepository;
    
    private TaskBoardEntity taskBoard; 

    @Autowired
    public TaskGroupEntityRepositoryIntegrationTests(TaskGroupRepository underTest,
                                                    TaskBoardRepository taskBoardRepository){
        this.underTest = underTest;
        this.taskBoardRepository = taskBoardRepository;
    }

    @BeforeEach
    public void setUp(){
        taskBoard = taskBoardRepository.save(
            TestDataUtil.createTestTaskBoardEntityA()
        ); 
    }

    @Test
    public void testThatEmptyTaskGroupCanBeCreatedAndRecalled(){
        TaskGroupEntity testTaskGroup = TestDataUtil.createTaskGroupEntityA(taskBoard);
        underTest.save(testTaskGroup);
        Optional<TaskGroupEntity> result = underTest.findById(testTaskGroup.getId());
        assertThat(result).isPresent();
        assertThat(result.get()).extracting(TaskGroupEntity::getId).isEqualTo(testTaskGroup.getId());
    }

    @Test
    public void testThatMultipleEmptyTaskGroupsCanBeCreatedAndRecalled(){
        TaskGroupEntity testTaskGroupA = TestDataUtil.createTaskGroupEntityA(taskBoard);
        underTest.save(testTaskGroupA);
        TaskGroupEntity testTaskGroupB = TestDataUtil.createTaskGroupEntityB(taskBoard);
        underTest.save(testTaskGroupB);
        TaskGroupEntity testTaskGroupC = TestDataUtil.createTaskGroupEntityB(taskBoard);
        underTest.save(testTaskGroupC);
        Iterable<TaskGroupEntity> result = underTest.findAll();
        assertThat(result)
                .extracting(TaskGroupEntity::getId)
                .hasSize(3)
                .containsExactly(testTaskGroupA.getId(), testTaskGroupB.getId(), testTaskGroupC.getId());
    }

    @Test
    public void testThatTaskGroupWithTasksCanBeCreatedAndRecalled(){
        TaskGroupEntity testTaskGroup = TestDataUtil.createTaskGroupEntityA(taskBoard);
        TaskEntity testTaskEntityA = TestDataUtil.createTestTaskEntityA(testTaskGroup);
        testTaskGroup.addTask(testTaskEntityA);

        underTest.save(testTaskGroup);
        
        Optional<TaskGroupEntity> result = underTest.findByIdWithTasks(1L);
        TaskGroupEntity savedGroup = result.get();
        assertThat(savedGroup.getTaskGroupName())
                            .isEqualTo(testTaskGroup.getTaskGroupName());
        assertThat(savedGroup.getTasks())
                            .hasSize(1);
        TaskEntity savedTask = savedGroup.getTasks().get(0);
        assertThat(savedTask.getTaskName())
                            .isEqualTo(testTaskEntityA.getTaskName());
    }

    @Test
    public void testThatTaskGroupCanBeUpdated(){
        TaskGroupEntity testTaskGroupA = TestDataUtil.createTaskGroupEntityA(taskBoard);
        underTest.save(testTaskGroupA);
        testTaskGroupA.setTaskGroupName("UPDATED");
        underTest.save(testTaskGroupA);

        Optional<TaskGroupEntity> result = underTest.findById(testTaskGroupA.getId());
        assertThat(result).isPresent();
        assertThat(result.get())
                .extracting(TaskGroupEntity::getTaskGroupName)
                .isEqualTo("UPDATED");
    }

    @Test
    public void testThatTaskGroupCanBeDeleted(){
        TaskGroupEntity testTaskGroupA = TestDataUtil.createTaskGroupEntityA(taskBoard);
        underTest.save(testTaskGroupA);

        underTest.deleteById(testTaskGroupA.getId());
        Optional<TaskGroupEntity> result = underTest.findById(testTaskGroupA.getId());
        assertThat(result).isEmpty();
    }
}
