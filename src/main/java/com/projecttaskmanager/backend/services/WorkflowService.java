// services/WorkflowService.java
package com.projecttaskmanager.backend.services;

import com.projecttaskmanager.backend.dto.request.workflow.CreateWorkflowRequest;
import com.projecttaskmanager.backend.dto.request.workflow.CreateWorkflowStepRequest;
import com.projecttaskmanager.backend.dto.response.workflow.WorkflowResponse;
import com.projecttaskmanager.backend.dto.response.workflow.WorkflowStepResponse;
import com.projecttaskmanager.backend.models.Project;
import com.projecttaskmanager.backend.models.TaskStatus;

import java.util.List;
import java.util.UUID;

public interface WorkflowService {

    WorkflowResponse createWorkflow(CreateWorkflowRequest request);

    WorkflowStepResponse createStep(CreateWorkflowStepRequest request);

    void validateTransition(UUID projectId, TaskStatus from, TaskStatus to);

    boolean hasWorkflow(UUID projectId);

    WorkflowResponse getWorkflowByProject(UUID projectId);

    List<WorkflowStepResponse> getWorkflowSteps(UUID workflowId);

    void deleteStep(UUID stepId);

    WorkflowResponse updateWorkflow(UUID workflowId, String name);

    void createDefaultWorkflow(Project project);
}