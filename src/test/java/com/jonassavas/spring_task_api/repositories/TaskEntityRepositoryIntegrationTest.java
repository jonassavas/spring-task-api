package com.jonassavas.spring_task_api.repositories;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import com.jonassavas.spring_task_api.TestDataUtil;
import com.jonassavas.spring_task_api.domain.entities.TaskBoardEntity;
import com.jonassavas.spring_task_api.domain.entities.TaskEntity;
import com.jonassavas.spring_task_api.domain.entities.TaskGroupEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TaskEntityRepositoryIntegrationTest {
    
    private TaskRepository underTest;

    private TaskBoardRepository taskBoardRepository;
    private TaskGroupRepository taskGroupRepository;

    private TaskBoardEntity testTaskBoard;
    private TaskGroupEntity testTaskGroup;

    // TODO
    @BeforeEach
    public void setUp(){
        //taskBoard = 
    }

    @Autowired
    public TaskEntityRepositoryIntegrationTest(TaskRepository underTest, 
                                            TaskBoardRepository taskBoardRepository, 
                                            TaskGroupRepository taskGroupRepository){
        this.underTest = underTest;
        this.taskBoardRepository = taskBoardRepository;
        this.taskGroupRepository = taskGroupRepository;
    }

    @Test
    public void testThatTaskCanBeCreatedAndRecalled(){
        TaskEntity testTaskA = TestDataUtil.createTestTaskEntityA(testTaskBoard);
        underTest.save(testTaskA);
        Optional<TaskEntity> result = underTest.findById(1L);
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testTaskA);
    }

    @Test
    public void testThatMultipleTasksCanBeCreatedAndRecalled(){
        TaskEntity testTaskA = TestDataUtil.createTestTaskEntityA();
        underTest.save(testTaskA);
        TaskEntity testTaskB = TestDataUtil.createTestTaskEntityB();
        underTest.save(testTaskB);
        TaskEntity testTaskC = TestDataUtil.createTestTaskEntityC();
        underTest.save(testTaskC);

        Iterable<TaskEntity> result = underTest.findAll();
        assertThat(result)
                .hasSize(3)
                .containsExactly(testTaskA, testTaskB, testTaskC);
    }

    @Test
    public void testThatTaskCanBeUpdated(){
        TaskEntity taskEntityA = TestDataUtil.createTestTaskEntityA();
        underTest.save(taskEntityA);
        taskEntityA.setTaskName("UPDATED");
        underTest.save(taskEntityA);
        
        Optional<TaskEntity> result = underTest.findById(taskEntityA.getId());
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(taskEntityA);
    }

    @Test
    public void testThatTaskCanBeDeleted(){
        TaskEntity taskEntityA = TestDataUtil.createTestTaskEntityA();
        underTest.save(taskEntityA);

        underTest.deleteById(taskEntityA.getId());
        Optional<TaskEntity> result = underTest.findById(taskEntityA.getId());
        assertThat(result).isEmpty();
    }

}
