package com.projecttaskmanager.backend.repositories;

import com.projecttaskmanager.backend.models.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskStatusRepository extends JpaRepository<TaskStatus, UUID> {
    Optional<TaskStatus> findByIsDefaultTrue();
    List<TaskStatus> findAllByOrderBySortAsc();
}