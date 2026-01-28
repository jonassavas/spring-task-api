package com.jonassavas.spring_task_api.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.jonassavas.spring_task_api.domain.dto.CreateTaskGroupDto;
import com.jonassavas.spring_task_api.domain.dto.TaskDto;
import com.jonassavas.spring_task_api.domain.dto.TaskGroupDto;
import com.jonassavas.spring_task_api.domain.entities.TaskEntity;
import com.jonassavas.spring_task_api.domain.entities.TaskGroupEntity;
import com.jonassavas.spring_task_api.mappers.Mapper;
import com.jonassavas.spring_task_api.services.TaskGroupService;
import com.jonassavas.spring_task_api.services.TaskService;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class TaskGroupController {
    private TaskGroupService taskGroupService;

    private Mapper<TaskGroupEntity, CreateTaskGroupDto> createTaskGroupMapper;
    private Mapper<TaskGroupEntity, TaskGroupDto> taskGroupMapper;

    public TaskGroupController(TaskGroupService taskGroupService, 
                                Mapper<TaskGroupEntity, CreateTaskGroupDto> createTaskGroupMapper,
                                Mapper<TaskGroupEntity, TaskGroupDto> taskGroupMapper){
        this.taskGroupService = taskGroupService;
        this.createTaskGroupMapper = createTaskGroupMapper;
        this.taskGroupMapper = taskGroupMapper;
    }

    @PostMapping(path = "/taskgroups")
    public ResponseEntity<CreateTaskGroupDto> createTaskGroup(@RequestBody CreateTaskGroupDto taskGroup) {
        TaskGroupEntity taskGroupEntity = createTaskGroupMapper.mapFrom(taskGroup);
        TaskGroupEntity savedTaskGroupEntity = taskGroupService.save(taskGroupEntity);
        return new ResponseEntity<>(createTaskGroupMapper.mapTo(savedTaskGroupEntity), HttpStatus.CREATED);
    }

    @GetMapping(path = "/taskgroups")
    public List<TaskGroupDto> listTaskGroups() {
        List<TaskGroupEntity> taskGroups = taskGroupService.findAll();
        return taskGroups.stream().map(taskGroupMapper::mapTo).collect(Collectors.toList());
    }

    @DeleteMapping(path = "/taskgroups/{id}")
    public ResponseEntity deleteTaskGroup(@PathVariable("id") Long id){
        taskGroupService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(path = "/taskgroups/{groupId}/tasks")
    public ResponseEntity deleteAllTasks(@PathVariable("groupId") Long groupId){
        taskGroupService.deleteAllTasks(groupId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
