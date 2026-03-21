package com.projecttaskmanager.backend.mapper;

import com.projecttaskmanager.backend.dto.response.workflow.WorkflowStepResponse;
import com.projecttaskmanager.backend.models.WorkflowStep;
import org.springframework.stereotype.Component;

@Component
public class WorkflowStepMapper {

    public WorkflowStepResponse toResponse(WorkflowStep step) {
        return WorkflowStepResponse.builder()
                .id(step.getId())
                .workflowId(step.getWorkflow().getId())
                .fromStatus(step.getFromStatus().getName())
                .toStatus(step.getToStatus().getName())
                .requiredPermission(step.getRequiredPermission())
                .build();
    }
}