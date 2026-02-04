package com.jonassavas.spring_task_api.mappers.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;


import com.jonassavas.spring_task_api.domain.dto.TaskGroupWithTasksDto;
import com.jonassavas.spring_task_api.domain.entities.TaskGroupEntity;
import com.jonassavas.spring_task_api.mappers.Mapper;

@Component
public class TaskGroupWithTasksMapper implements Mapper<TaskGroupEntity, TaskGroupWithTasksDto>{
    private ModelMapper modelMapper;

    public TaskGroupWithTasksMapper(ModelMapper modelMapper){
        this.modelMapper = modelMapper;
    }
    
    @Override
    public TaskGroupWithTasksDto mapTo(TaskGroupEntity taskGroupEntity){
        return modelMapper.map(taskGroupEntity, TaskGroupWithTasksDto.class);
    }

    @Override
    public TaskGroupEntity mapFrom(TaskGroupWithTasksDto taskGroupDto){
        return modelMapper.map(taskGroupDto, TaskGroupEntity.class);
    }
}
