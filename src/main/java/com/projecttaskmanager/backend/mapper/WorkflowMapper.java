package com.projecttaskmanager.backend.mapper;

import com.projecttaskmanager.backend.dto.response.workflow.WorkflowResponse;
import com.projecttaskmanager.backend.models.Workflow;
import org.springframework.stereotype.Component;

@Component
public class WorkflowMapper {

    public WorkflowResponse toResponse(Workflow wf) {
        return WorkflowResponse.builder()
                .id(wf.getId())
                .name(wf.getName())
                .projectId(wf.getProject().getId())
                .build();
    }
}