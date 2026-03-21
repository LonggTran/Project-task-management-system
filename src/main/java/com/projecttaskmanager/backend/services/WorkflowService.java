package com.projecttaskmanager.backend.services;

import com.projecttaskmanager.backend.dto.request.workflow.CreateWorkflowRequest;
import com.projecttaskmanager.backend.dto.request.workflow.CreateWorkflowStepRequest;
import com.projecttaskmanager.backend.dto.response.workflow.WorkflowResponse;
import com.projecttaskmanager.backend.dto.response.workflow.WorkflowStepResponse;
import com.projecttaskmanager.backend.models.TaskStatus;

import java.util.UUID;

public interface WorkflowService {

    WorkflowResponse createWorkflow(CreateWorkflowRequest request);

    WorkflowStepResponse createStep(CreateWorkflowStepRequest request);

    void validateTransition(UUID projectId,
                            TaskStatus from,
                            TaskStatus to);
}