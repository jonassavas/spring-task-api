package com.jonassavas.spring_task_api.services;

import com.jonassavas.spring_task_api.domain.entities.TaskEntity;
import com.jonassavas.spring_task_api.domain.entities.TaskSortMode;
import java.util.Comparator;
import java.util.List;

public final class TaskOrdering {
    private TaskOrdering() {}

    public static List<TaskEntity> order(List<TaskEntity> tasks, TaskSortMode mode) {
        Comparator<TaskEntity> manual =
                Comparator.comparing(TaskEntity::getPosition).thenComparing(TaskEntity::getId);
        Comparator<TaskEntity> comparator =
                mode == TaskSortMode.DUE_DATE_ASC
                        ? Comparator.comparing(
                                        TaskEntity::getDueDate,
                                        Comparator.nullsLast(Comparator.naturalOrder()))
                                .thenComparing(manual)
                        : manual;
        return tasks.stream().sorted(comparator).toList();
    }
}
