package com.projecttaskmanager.backend.dto.response.workflow;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class WorkflowResponse {
    private UUID id;
    private String name;
    private UUID projectId;
}