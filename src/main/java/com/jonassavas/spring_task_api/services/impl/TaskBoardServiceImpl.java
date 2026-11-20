package com.jonassavas.spring_task_api.services.impl;

import com.jonassavas.spring_task_api.domain.dto.task_board.CreateTaskBoardRequestDto;
import com.jonassavas.spring_task_api.domain.dto.task_board.ReorderTaskBoardsRequestDto;
import com.jonassavas.spring_task_api.domain.dto.task_board.TaskBoardDto;
import com.jonassavas.spring_task_api.domain.dto.task_board.TaskBoardWithGroupsDto;
import com.jonassavas.spring_task_api.domain.dto.task_board.UpdateTaskBoardRequestDto;
import com.jonassavas.spring_task_api.domain.entities.TaskBoardEntity;
import com.jonassavas.spring_task_api.domain.entities.TaskGroupEntity;
import com.jonassavas.spring_task_api.domain.entities.TaskSortMode;
import com.jonassavas.spring_task_api.domain.entities.UserEntity;
import com.jonassavas.spring_task_api.mappers.impl.task_board.TaskBoardMapper;
import com.jonassavas.spring_task_api.repositories.TaskBoardRepository;
import com.jonassavas.spring_task_api.repositories.UserRepository;
import com.jonassavas.spring_task_api.security.SecurityService;
import com.jonassavas.spring_task_api.services.TaskBoardService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class TaskBoardServiceImpl implements TaskBoardService {

    private TaskBoardRepository taskBoardRepository;
    private final TaskBoardMapper taskBoardMapper;
    private final SecurityService securityService;
    private final UserRepository userRepository;

    public TaskBoardServiceImpl(
            TaskBoardRepository taskBoardRepository,
            TaskBoardMapper taskBoardMapper,
            SecurityService securityService,
            UserRepository userRepository) {
        this.taskBoardRepository = taskBoardRepository;
        this.taskBoardMapper = taskBoardMapper;
        this.securityService = securityService;
        this.userRepository = userRepository;
    }

    @Override
    public TaskBoardDto createTaskBoard(CreateTaskBoardRequestDto requestDto) {
        UserEntity user = securityService.getCurrentUser();
        userRepository.findByIdForUpdate(user.getId()).orElseThrow();
        Integer maxPosition =
                taskBoardRepository.findMaxPositionByOwnerUsername(user.getUsername());
        TaskBoardEntity taskBoard =
                TaskBoardEntity.builder()
                        .taskBoardName(requestDto.getTaskBoardName())
                        .position(maxPosition == null ? 0 : maxPosition + 1)
                        .owner(user)
                        .build();
        if (requestDto.isUseDefaultConfiguration()) {
            List<String> groupNames =
                    List.of("Done", "Today", "Tomorrow", "This Week", "Deadlines");
            for (int position = 0; position < groupNames.size(); position++) {
                taskBoard.addTaskGroup(
                        TaskGroupEntity.builder()
                                .taskGroupName(groupNames.get(position))
                                .position(position)
                                .strikeThroughTasks(position == 0)
                                .sortMode(
                                        position == 4
                                                ? TaskSortMode.DUE_DATE_ASC
                                                : TaskSortMode.MANUAL)
                                .build());
            }
        }
        TaskBoardEntity savedTaskBoard = taskBoardRepository.save(taskBoard);
        return taskBoardMapper.mapTo(savedTaskBoard);
    }

    @Override
    public TaskBoardDto update(Long id, UpdateTaskBoardRequestDto requestDto) {
        UserEntity user = securityService.getCurrentUser();

        TaskBoardEntity board =
                taskBoardRepository
                        .findByIdAndOwnerUsername(id, user.getUsername())
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                "TaskBoard not found with id: "
                                                        + id
                                                        + " for user: "
                                                        + user.getUsername()));

        if (requestDto.getTaskBoardName() != null) {
            board.setTaskBoardName(requestDto.getTaskBoardName());
        }

        return taskBoardMapper.mapTo(board);
    }

    @Override
    public void delete(Long id) {
        UserEntity user = securityService.getCurrentUser();

        TaskBoardEntity board =
                taskBoardRepository
                        .findByIdAndOwnerUsername(id, user.getUsername())
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                "TaskBoard not found or not owned by user"));

        taskBoardRepository.delete(board);
    }

    @Override
    public Optional<TaskBoardDto> findById(Long id) {

        UserEntity user = securityService.getCurrentUser();

        Optional<TaskBoardEntity> taskBoard =
                taskBoardRepository.findByIdAndOwnerUsername(id, user.getUsername());

        return taskBoard.map(taskBoardMapper::mapTo);
    }

    @Override
    public TaskBoardWithGroupsDto getTaskBoardWithDetails(Long boardId) {

        UserEntity user = securityService.getCurrentUser();

        TaskBoardEntity board =
                taskBoardRepository
                        .findByIdAndOwnerUsername(boardId, user.getUsername())
                        .orElseThrow(() -> new EntityNotFoundException("TaskBoard not found"));

        // Second query: groups + their tasks (Hibernate handles one bag at a time fine)
        // Force initialization of tasks for each group separately
        board.getTaskGroups().forEach(group -> Hibernate.initialize(group.getTasks()));

        return taskBoardMapper.mapToWithGroups(board);
    }

    @Override
    public List<TaskBoardDto> listTaskBoardsForCurrentUser() {

        UserEntity user = securityService.getCurrentUser();

        return taskBoardRepository
                .findByOwnerUsernameOrderByPositionAsc(user.getUsername())
                .stream()
                .map(taskBoardMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isExist(Long id) {
        return taskBoardRepository.existsById(id);
    }

    @Override
    public void reorderTaskBoards(ReorderTaskBoardsRequestDto dto) {
        UserEntity user = securityService.getCurrentUser();
        userRepository.findByIdForUpdate(user.getId()).orElseThrow();
        List<TaskBoardEntity> boards =
                taskBoardRepository.findByOwnerUsernameOrderByPositionAsc(user.getUsername());
        List<Long> ids = dto.getBoardIds();
        Set<Long> uniqueIds = Set.copyOf(ids);
        if (ids.size() != boards.size()
                || uniqueIds.size() != ids.size()
                || !uniqueIds.equals(
                        boards.stream().map(TaskBoardEntity::getId).collect(Collectors.toSet()))) {
            throw new IllegalArgumentException(
                    "Board IDs must contain every owned board exactly once");
        }
        Map<Long, TaskBoardEntity> byId =
                boards.stream()
                        .collect(Collectors.toMap(TaskBoardEntity::getId, Function.identity()));
        for (int i = 0; i < boards.size(); i++) {
            boards.get(i).setPosition(-i - 1);
        }
        taskBoardRepository.flush();
        for (int i = 0; i < ids.size(); i++) {
            byId.get(ids.get(i)).setPosition(i);
        }
    }
}
