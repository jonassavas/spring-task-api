package com.jonassavas.spring_task_api.repositories;

import com.jonassavas.spring_task_api.domain.entities.TaskBoardEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskBoardRepository extends JpaRepository<TaskBoardEntity, Long> {
    List<TaskBoardEntity> findByOwnerUsername(String username);

    List<TaskBoardEntity> findByOwnerUsernameOrderByPositionAsc(String username);

    Optional<TaskBoardEntity> findByIdAndOwnerUsername(Long id, String username);

    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from TaskBoardEntity b where b.id = :id and b.owner.username = :username")
    Optional<TaskBoardEntity> findByIdAndOwnerUsernameForUpdate(Long id, String username);

    @Query("select max(b.position) from TaskBoardEntity b where b.owner.username = :username")
    Integer findMaxPositionByOwnerUsername(String username);
}
