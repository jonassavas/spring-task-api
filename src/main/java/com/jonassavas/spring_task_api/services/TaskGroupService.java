package com.jonassavas.spring_task_api.services;

import java.util.List;

import com.jonassavas.spring_task_api.domain.dto.TaskGroupRequestDto;
import com.jonassavas.spring_task_api.domain.entities.TaskGroupEntity;

public interface TaskGroupService {
    TaskGroupEntity save(TaskGroupEntity taskGroupEntity);

    List<TaskGroupEntity> findAll();

    List<TaskGroupEntity> findAllWithTasks();

    TaskGroupEntity findByIdWithTasks(Long id);

    boolean isExist(Long id);

    void delete(Long id);

    void deleteAllTasks(Long id);

    TaskGroupEntity update(Long id, TaskGroupRequestDto dto);
}
