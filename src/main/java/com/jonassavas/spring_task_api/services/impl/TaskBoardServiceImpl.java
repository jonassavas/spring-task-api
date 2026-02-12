package com.jonassavas.spring_task_api.services.impl;

import org.springframework.stereotype.Service;

import com.jonassavas.spring_task_api.domain.entities.TaskBoardEntity;
import com.jonassavas.spring_task_api.repositories.TaskBoardRepository;
import com.jonassavas.spring_task_api.services.TaskBoardService;

import jakarta.transaction.Transactional;

@Service
@Transactional
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

    @Override
    public boolean isExist(Long id){
        return taskBoardRepository.existsById(id);
    }
}
