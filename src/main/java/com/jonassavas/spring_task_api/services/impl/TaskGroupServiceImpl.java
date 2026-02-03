package com.jonassavas.spring_task_api.services.impl;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Service;

import com.jonassavas.spring_task_api.domain.dto.TaskGroupRequestDto;
import com.jonassavas.spring_task_api.domain.entities.TaskGroupEntity;
import com.jonassavas.spring_task_api.repositories.TaskGroupRepository;
import com.jonassavas.spring_task_api.services.TaskGroupService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class TaskGroupServiceImpl implements TaskGroupService{
    
    private TaskGroupRepository taskGroupRepository;

    public TaskGroupServiceImpl(TaskGroupRepository taskGroupRepository){
        this.taskGroupRepository = taskGroupRepository;
    }

    @Override
    public TaskGroupEntity save(TaskGroupEntity taskGroupEntity){
        return taskGroupRepository.save(taskGroupEntity);
    }

    @Override
    public List<TaskGroupEntity> findAll(){
        return StreamSupport.stream(taskGroupRepository
                                    .findAll()
                                    .spliterator(), false)
                                    .collect(Collectors.toList());
    }

    @Override
    public List<TaskGroupEntity> findAllWithTasks(){
        return taskGroupRepository.findAllWithTasks();
    }

    @Override
    public TaskGroupEntity findByIdWithTasks(Long id){
        return taskGroupRepository.findByIdWithTasks(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "TaskGroup not found with id " + id));
    }

    @Override
    public boolean isExist(Long id){
        return taskGroupRepository.existsById(id);
    }

    @Transactional
    @Override
    public void delete(Long id){
        taskGroupRepository.deleteById(id);
    }

    @Transactional
    @Override
    public void deleteAllTasks(Long id){
        TaskGroupEntity taskGroup = taskGroupRepository.findById(id)
                                    .orElseThrow(() -> new EntityNotFoundException(
                                        "TaskGroup not found with id " + id));

        taskGroup.getTasks().clear();
    }

    @Transactional
    @Override
    public TaskGroupEntity update(Long id, TaskGroupRequestDto dto){
        TaskGroupEntity taskGroup = taskGroupRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException(
                                        "TaskGroup not found with it: " + id));
    
        if(dto.getTaskGroupName() != null){
            taskGroup.setTaskGroupName(dto.getTaskGroupName());
        }

        return taskGroup;
    }
}
