package com.projecttaskmanager.backend.repositories;

import com.projecttaskmanager.backend.models.Project;
import com.projecttaskmanager.backend.models.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WorkflowRepository extends JpaRepository<Workflow, UUID> {
    Optional<Workflow> findByProject(Project project);
}