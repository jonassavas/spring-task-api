package com.jonassavas.spring_task_api.mappers.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.jonassavas.spring_task_api.domain.dto.CreateTaskDto;
import com.jonassavas.spring_task_api.domain.dto.TaskDto;
import com.jonassavas.spring_task_api.domain.entities.TaskEntity;
import com.jonassavas.spring_task_api.mappers.Mapper;

@Component
public class CreateTaskMapper implements Mapper<TaskEntity, CreateTaskDto> {

    private ModelMapper modelMapper;

    public CreateTaskMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;

        // Skip taskGroup when mapping DTO -> Entity
        this.modelMapper.typeMap(CreateTaskDto.class, TaskEntity.class)
                .addMappings(mapper -> mapper.skip(TaskEntity::setTaskGroup));
    }

    @Override
    public CreateTaskDto mapTo(TaskEntity taskEntity) {
        CreateTaskDto dto = modelMapper.map(taskEntity, CreateTaskDto.class);
        //dto.setTaskGroupId(taskEntity.getTaskGroup().getId());
        return dto;
    }

    @Override
    public TaskEntity mapFrom(CreateTaskDto createTaskDto) {
        return modelMapper.map(createTaskDto, TaskEntity.class);
    }
}

