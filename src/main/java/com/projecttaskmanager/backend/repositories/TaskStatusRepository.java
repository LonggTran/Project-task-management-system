// repositories/TaskStatusRepository.java
package com.projecttaskmanager.backend.repositories;

import com.projecttaskmanager.backend.models.Project;
import com.projecttaskmanager.backend.models.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskStatusRepository extends JpaRepository<TaskStatus, UUID> {
    List<TaskStatus> findAllByOrderBySortAsc();
    List<TaskStatus> findByProjectOrderBySortAsc(Project project);
    Optional<TaskStatus> findByNameAndProject(String name, Project project);
    Optional<TaskStatus> findByIsDefaultTrueAndProject(Project project);
    Optional<TaskStatus> findByIdAndProject(UUID id, Project project);
    Optional<TaskStatus> findByProjectIdAndIsDefaultTrue(UUID projectId);
}