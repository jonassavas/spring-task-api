package com.jonassavas.spring_task_api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jonassavas.spring_task_api.domain.entities.TaskBoardEntity;

public interface TaskBoardRepository extends JpaRepository<TaskBoardEntity, Long> {
    
}
