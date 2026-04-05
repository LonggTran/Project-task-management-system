package com.projecttaskmanager.backend.repositories;

import com.projecttaskmanager.backend.models.Task;
import com.projecttaskmanager.backend.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findAllByProjectId(UUID projectId);
    List<Task> findAllByParentTaskId(UUID parentTaskId);
    boolean existsByParentTaskId(UUID parentTaskId);
}