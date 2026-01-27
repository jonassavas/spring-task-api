package com.jonassavas.spring_task_api.services;

import java.util.List;

import com.jonassavas.spring_task_api.domain.entities.TaskGroupEntity;

public interface TaskGroupService {
    TaskGroupEntity save(TaskGroupEntity taskGroupEntity);

    List<TaskGroupEntity> findAll();

    boolean isExist(Long id);

    void delete(Long id);
}
