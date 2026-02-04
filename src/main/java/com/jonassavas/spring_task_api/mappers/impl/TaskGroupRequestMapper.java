package com.jonassavas.spring_task_api.mappers.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;


import com.jonassavas.spring_task_api.domain.dto.TaskGroupRequestDto;
import com.jonassavas.spring_task_api.domain.entities.TaskGroupEntity;
import com.jonassavas.spring_task_api.mappers.Mapper;

@Component
public class TaskGroupRequestMapper implements Mapper<TaskGroupEntity, TaskGroupRequestDto>{
    private ModelMapper modelMapper;

    public TaskGroupRequestMapper(ModelMapper modelMapper){
        this.modelMapper = modelMapper;
    }
    
    @Override
    public TaskGroupRequestDto mapTo(TaskGroupEntity taskGroupEntity){
        return modelMapper.map(taskGroupEntity, TaskGroupRequestDto.class);
    }

    @Override
    public TaskGroupEntity mapFrom(TaskGroupRequestDto taskGroupDto){
        return modelMapper.map(taskGroupDto, TaskGroupEntity.class);
    }
}
