package com.jonassavas.spring_task_api.services.impl;

import com.jonassavas.spring_task_api.domain.dto.task.CreateTaskRequestDto;
import com.jonassavas.spring_task_api.domain.dto.task.ReorderTasksRequestDto;
import com.jonassavas.spring_task_api.domain.dto.task.TaskDto;
import com.jonassavas.spring_task_api.domain.dto.task.UpdateTaskRequestDto;
import com.jonassavas.spring_task_api.domain.entities.TaskEntity;
import com.jonassavas.spring_task_api.domain.entities.TaskGroupEntity;
import com.jonassavas.spring_task_api.domain.entities.TaskSortMode;
import com.jonassavas.spring_task_api.exceptions.ConflictException;
import com.jonassavas.spring_task_api.mappers.Mapper;
import com.jonassavas.spring_task_api.repositories.TaskGroupRepository;
import com.jonassavas.spring_task_api.repositories.TaskRepository;
import com.jonassavas.spring_task_api.security.SecurityService;
import com.jonassavas.spring_task_api.services.TaskOrdering;
import com.jonassavas.spring_task_api.services.TaskService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final TaskGroupRepository taskGroupRepository;
    private final Mapper<TaskEntity, TaskDto> taskMapper;
    private final SecurityService securityService;

    public TaskServiceImpl(
            TaskRepository taskRepository,
            TaskGroupRepository taskGroupRepository,
            Mapper<TaskEntity, TaskDto> taskMapper,
            SecurityService securityService) {
        this.taskRepository = taskRepository;
        this.taskGroupRepository = taskGroupRepository;
        this.taskMapper = taskMapper;
        this.securityService = securityService;
    }

    @Override
    public TaskDto createTask(Long groupId, CreateTaskRequestDto dto) {
        TaskGroupEntity group = ownedGroupForUpdate(groupId);
        Integer maxPosition = taskRepository.findMaxPositionByTaskGroupId(groupId);
        TaskEntity task =
                TaskEntity.builder()
                        .taskName(dto.getTaskName())
                        .dueDate(dto.getDueDate())
                        .position(maxPosition == null ? 0 : maxPosition + 1)
                        .taskGroup(group)
                        .build();
        return taskMapper.mapTo(taskRepository.save(task));
    }

    @Override
    public void delete(Long id) {
        String username = securityService.getCurrentUsername();
        TaskEntity task =
                taskRepository
                        .findByIdAndTaskGroupTaskBoardOwnerUsername(id, username)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                "Task not found or not owned by user"));
        task.getTaskGroup().removeTask(task);
    }

    @Override
    public boolean isExist(Long id) {
        return taskRepository.existsById(id);
    }

    @Override
    public List<TaskDto> findByGroup(Long groupId) {
        TaskGroupEntity group = ownedGroup(groupId);
        return TaskOrdering.order(
                        taskRepository
                                .findByTaskGroupIdAndTaskGroupTaskBoardOwnerUsernameOrderByPositionAsc(
                                        groupId, securityService.getCurrentUsername()),
                        group.getSortMode())
                .stream()
                .map(taskMapper::mapTo)
                .toList();
    }

    @Override
    public TaskDto update(Long id, UpdateTaskRequestDto dto) {
        String username = securityService.getCurrentUsername();
        TaskEntity task =
                taskRepository
                        .findByIdAndTaskGroupTaskBoardOwnerUsername(id, username)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                "Task not found or not owned by user"));

        if (dto.getTaskName() != null) {
            task.setTaskName(dto.getTaskName());
        }
        if (dto.isDueDateSupplied()) {
            task.setDueDate(dto.getDueDate());
        }
        if (dto.getTaskGroupId() != null
                && !dto.getTaskGroupId().equals(task.getTaskGroup().getId())) {
            TaskGroupEntity destination = ownedGroupForUpdate(dto.getTaskGroupId());
            Integer maxPosition = taskRepository.findMaxPositionByTaskGroupId(destination.getId());
            task.setPosition(maxPosition == null ? 0 : maxPosition + 1);
            task.setTaskGroup(destination);
        }
        return taskMapper.mapTo(task);
    }

    @Override
    public void reorderTasks(ReorderTasksRequestDto dto) {
        Map<Long, TaskGroupEntity> groups = new HashMap<>();
        List<Long> groupIds =
                dto.getSourceGroupId().equals(dto.getDestinationGroupId())
                        ? List.of(dto.getSourceGroupId())
                        : List.of(dto.getSourceGroupId(), dto.getDestinationGroupId()).stream()
                                .sorted()
                                .toList();
        for (Long groupId : groupIds) {
            groups.put(groupId, ownedGroupForUpdate(groupId));
        }
        TaskGroupEntity source = groups.get(dto.getSourceGroupId());
        TaskGroupEntity destination = groups.get(dto.getDestinationGroupId());
        if (source.getSortMode() != TaskSortMode.MANUAL
                || destination.getSortMode() != TaskSortMode.MANUAL) {
            throw new ConflictException(
                    "Tasks in a date-sorted group cannot be reordered manually");
        }

        String username = securityService.getCurrentUsername();
        List<TaskEntity> sourceTasks =
                taskRepository
                        .findByTaskGroupIdAndTaskGroupTaskBoardOwnerUsernameOrderByPositionAsc(
                                source.getId(), username);
        boolean sameGroup = source.getId().equals(destination.getId());
        List<TaskEntity> destinationTasks =
                sameGroup
                        ? List.of()
                        : taskRepository
                                .findByTaskGroupIdAndTaskGroupTaskBoardOwnerUsernameOrderByPositionAsc(
                                        destination.getId(), username);
        validateReorder(dto, sourceTasks, destinationTasks, sameGroup);

        List<TaskEntity> allTasks = new ArrayList<>(sourceTasks);
        allTasks.addAll(destinationTasks);
        for (int i = 0; i < allTasks.size(); i++) {
            allTasks.get(i).setPosition(-i - 1);
        }
        taskRepository.flush();

        Map<Long, TaskEntity> byId =
                allTasks.stream().collect(Collectors.toMap(TaskEntity::getId, task -> task));
        for (int i = 0; i < dto.getSourceTaskIds().size(); i++) {
            byId.get(dto.getSourceTaskIds().get(i)).setPosition(i);
        }
        if (!sameGroup) {
            for (int i = 0; i < dto.getDestinationTaskIds().size(); i++) {
                TaskEntity task = byId.get(dto.getDestinationTaskIds().get(i));
                task.setTaskGroup(destination);
                task.setPosition(i);
            }
        }
    }

    private void validateReorder(
            ReorderTasksRequestDto dto,
            List<TaskEntity> sourceTasks,
            List<TaskEntity> destinationTasks,
            boolean sameGroup) {
        List<Long> sourceIds = dto.getSourceTaskIds();
        List<Long> destinationIds = dto.getDestinationTaskIds();
        Set<Long> currentSourceIds =
                sourceTasks.stream().map(TaskEntity::getId).collect(Collectors.toSet());
        if (sameGroup) {
            if (!exactPermutation(sourceIds, currentSourceIds)
                    || (!destinationIds.isEmpty() && !destinationIds.equals(sourceIds))) {
                throw new IllegalArgumentException(
                        "Task IDs must contain every task in the group exactly once");
            }
            return;
        }

        Set<Long> currentDestinationIds =
                destinationTasks.stream().map(TaskEntity::getId).collect(Collectors.toSet());
        List<Long> combinedIds = new ArrayList<>(sourceIds);
        combinedIds.addAll(destinationIds);
        Set<Long> allCurrentIds = new HashSet<>(currentSourceIds);
        allCurrentIds.addAll(currentDestinationIds);
        if (!currentSourceIds.containsAll(sourceIds)
                || !destinationIds.containsAll(currentDestinationIds)
                || !exactPermutation(combinedIds, allCurrentIds)) {
            throw new IllegalArgumentException(
                    "Task IDs must contain every task in both groups exactly once");
        }
    }

    private boolean exactPermutation(List<Long> ids, Set<Long> expected) {
        return ids.size() == expected.size() && new HashSet<>(ids).equals(expected);
    }

    private TaskGroupEntity ownedGroup(Long id) {
        return taskGroupRepository
                .findByIdAndTaskBoardOwnerUsername(id, securityService.getCurrentUsername())
                .orElseThrow(
                        () ->
                                new EntityNotFoundException(
                                        "TaskGroup not found or not owned by user"));
    }

    private TaskGroupEntity ownedGroupForUpdate(Long id) {
        return taskGroupRepository
                .findByIdAndTaskBoardOwnerUsernameForUpdate(
                        id, securityService.getCurrentUsername())
                .orElseThrow(
                        () ->
                                new EntityNotFoundException(
                                        "TaskGroup not found or not owned by user"));
    }
}
