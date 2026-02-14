package com.jonassavas.spring_task_api.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.jonassavas.spring_task_api.services.TaskGroupService;

@RestController
public class TaskBoardController {

    private TaskGroupService taskGroupService;

    public TaskBoardController(){

    }

    // POST /boards
    // GET /boards (used for selecting which board)
    // GET /boards/{boardId}


    
}
