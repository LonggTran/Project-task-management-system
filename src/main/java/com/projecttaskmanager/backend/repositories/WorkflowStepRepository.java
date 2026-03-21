package com.projecttaskmanager.backend.repositories;

import com.projecttaskmanager.backend.models.TaskStatus;
import com.projecttaskmanager.backend.models.Workflow;
import com.projecttaskmanager.backend.models.WorkflowStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WorkflowStepRepository extends JpaRepository<WorkflowStep, UUID> {

    Optional<WorkflowStep> findByWorkflowAndFromStatusAndToStatus(
            Workflow workflow,
            TaskStatus fromStatus,
            TaskStatus toStatus
    );
}