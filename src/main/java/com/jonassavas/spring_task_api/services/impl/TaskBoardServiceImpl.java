package com.jonassavas.spring_task_api.services.impl;

import com.jonassavas.spring_task_api.domain.entities.TaskBoardEntity;
import com.jonassavas.spring_task_api.repositories.TaskBoardRepository;
import com.jonassavas.spring_task_api.services.TaskBoardService;

public class TaskBoardServiceImpl implements TaskBoardService {

    private TaskBoardRepository taskBoardRepository;

    public TaskBoardServiceImpl(TaskBoardRepository taskBoardRepository){
        this.taskBoardRepository = taskBoardRepository;
    }

    @Override
    public TaskBoardEntity save(TaskBoardEntity taskBoard){
         return taskBoardRepository.save(taskBoard);
    } 

    @Override
    public TaskBoardEntity update(Long id, TaskBoardEntity taskBoard){
        return null;
    }

    @Override
    public void delete(Long id){
        
    }

    @Override
    public TaskBoardEntity findById(Long id){
        return null;
    }
}
