package com.projecttaskmanager.backend.services.impl;

import com.projecttaskmanager.backend.dto.request.workflow.CreateWorkflowRequest;
import com.projecttaskmanager.backend.dto.request.workflow.CreateWorkflowStepRequest;
import com.projecttaskmanager.backend.dto.response.workflow.WorkflowResponse;
import com.projecttaskmanager.backend.dto.response.workflow.WorkflowStepResponse;
import com.projecttaskmanager.backend.exceptions.AppException;
import com.projecttaskmanager.backend.exceptions.ErrorCode;
import com.projecttaskmanager.backend.mapper.WorkflowMapper;
import com.projecttaskmanager.backend.mapper.WorkflowStepMapper;
import com.projecttaskmanager.backend.models.Project;
import com.projecttaskmanager.backend.models.TaskStatus;
import com.projecttaskmanager.backend.models.Workflow;
import com.projecttaskmanager.backend.models.WorkflowStep;
import com.projecttaskmanager.backend.repositories.ProjectRepository;
import com.projecttaskmanager.backend.repositories.TaskStatusRepository;
import com.projecttaskmanager.backend.repositories.WorkflowRepository;
import com.projecttaskmanager.backend.repositories.WorkflowStepRepository;
import com.projecttaskmanager.backend.services.ProjectAuthorizationService;
import com.projecttaskmanager.backend.services.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowServiceImpl implements WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowStepRepository stepRepository;
    private final ProjectRepository projectRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final WorkflowMapper workflowMapper;
    private final WorkflowStepMapper stepMapper;
    private final ProjectAuthorizationService authorizationService;

    @Override
    public WorkflowResponse createWorkflow(CreateWorkflowRequest request) {

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        authorizationService.checkPermission(project.getId(), "PROJECT_UPDATE");

        Workflow wf = Workflow.builder()
                .name(request.getName())
                .project(project)
                .build();

        return workflowMapper.toResponse(workflowRepository.save(wf));
    }

    @Override
    public WorkflowStepResponse createStep(CreateWorkflowStepRequest request) {

        Workflow wf = workflowRepository.findById(request.getWorkflowId())
                .orElseThrow(() -> new RuntimeException("Workflow not found"));

        authorizationService.checkPermission(wf.getProject().getId(), "PROJECT_UPDATE");

        TaskStatus from = taskStatusRepository.findById(request.getFromStatusId())
                .orElseThrow(() -> new RuntimeException("Status not found"));

        TaskStatus to = taskStatusRepository.findById(request.getToStatusId())
                .orElseThrow(() -> new RuntimeException("Status not found"));

        WorkflowStep step = WorkflowStep.builder()
                .workflow(wf)
                .fromStatus(from)
                .toStatus(to)
                .requiredPermission(request.getRequiredPermission())
                .build();

        return stepMapper.toResponse(stepRepository.save(step));
    }

    // 🔥 CORE VALIDATION
    @Override
    public void validateTransition(UUID projectId,
                                   TaskStatus from,
                                   TaskStatus to) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        Workflow workflow = workflowRepository.findByProject(project)
                .orElseThrow(() -> new RuntimeException("Workflow not found"));

        WorkflowStep step = stepRepository
                .findByWorkflowAndFromStatusAndToStatus(workflow, from, to)
                .orElseThrow(() -> new AppException(ErrorCode.FORBIDDEN));

        // check permission
        authorizationService.checkPermission(projectId, step.getRequiredPermission());
    }
}