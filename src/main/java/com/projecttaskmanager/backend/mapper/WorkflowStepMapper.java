// mappers/WorkflowStepMapper.java
package com.projecttaskmanager.backend.mapper;

import com.projecttaskmanager.backend.dto.response.workflow.WorkflowStepResponse;
import com.projecttaskmanager.backend.models.WorkflowStep;
import org.springframework.stereotype.Component;

@Component
public class WorkflowStepMapper {

    public WorkflowStepResponse toResponse(WorkflowStep step) {
        if (step == null) return null;

        return WorkflowStepResponse.builder()
                .id(step.getId())
                .workflowId(step.getWorkflow().getId())
                .fromStatus(step.getFromStatus().getName())
                .fromStatusId(step.getFromStatus().getId())
                .toStatus(step.getToStatus().getName())
                .toStatusId(step.getToStatus().getId())
                .requiredPermission(step.getRequiredPermission())
                .build();
    }
}