// services/impl/WorkflowServiceImpl.java
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

//        authorizationService.checkPermission(project.getId(), "PROJECT_UPDATE");

        Workflow wf = Workflow.builder()
                .name(request.getName())
                .project(project)
                .build();

        return workflowMapper.toResponse(workflowRepository.save(wf));
    }

    @Override
    public WorkflowStepResponse createStep(CreateWorkflowStepRequest request) {
        Workflow wf = workflowRepository.findById(request.getWorkflowId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

//        authorizationService.checkPermission(wf.getProject().getId(), "PROJECT_UPDATE");

        TaskStatus from = taskStatusRepository.findById(request.getFromStatusId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        TaskStatus to = taskStatusRepository.findById(request.getToStatusId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        // Kiểm tra step đã tồn tại
        if (stepRepository.findByWorkflowAndFromStatusAndToStatus(wf, from, to).isPresent()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }

        WorkflowStep step = WorkflowStep.builder()
                .workflow(wf)
                .fromStatus(from)
                .toStatus(to)
                .requiredPermission(request.getRequiredPermission())
                .build();

        return stepMapper.toResponse(stepRepository.save(step));
    }

    @Override
    public void validateTransition(UUID projectId, TaskStatus from, TaskStatus to) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        Workflow workflow = workflowRepository.findByProject(project)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        WorkflowStep step = stepRepository
                .findByWorkflowAndFromStatusAndToStatus(workflow, from, to)
                .orElseThrow(() -> new AppException(ErrorCode.FORBIDDEN));

//        authorizationService.checkPermission(projectId, step.getRequiredPermission());
    }

    @Override
    public boolean hasWorkflow(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));
        return workflowRepository.findByProject(project).isPresent();
    }

    @Override
    public WorkflowResponse getWorkflowByProject(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        Workflow workflow = workflowRepository.findByProject(project)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        return workflowMapper.toResponse(workflow);
    }

    @Override
    public List<WorkflowStepResponse> getWorkflowSteps(UUID workflowId) {
        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

//        authorizationService.checkPermission(workflow.getProject().getId(), "PROJECT_VIEW");

        return stepRepository.findByWorkflow(workflow)
                .stream()
                .map(stepMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteStep(UUID stepId) {
        WorkflowStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

//        authorizationService.checkPermission(step.getWorkflow().getProject().getId(), "PROJECT_UPDATE");

        stepRepository.delete(step);
    }

    @Override
    public WorkflowResponse updateWorkflow(UUID workflowId, String name) {
        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

//        authorizationService.checkPermission(workflow.getProject().getId(), "PROJECT_UPDATE");

        workflow.setName(name);
        workflowRepository.save(workflow);

        return workflowMapper.toResponse(workflow);
    }

    @Override
    @Transactional
    public void createDefaultWorkflow(Project project) {
        // Tạo workflow mặc định
        Workflow defaultWorkflow = Workflow.builder()
                .name("Default Workflow")
                .project(project)
                .build();
        workflowRepository.save(defaultWorkflow);

        List<TaskStatus> statuses = taskStatusRepository.findByProjectOrderBySortAsc(project);

        for (int i = 0; i < statuses.size() - 1; i++) {
            WorkflowStep step = WorkflowStep.builder()
                    .workflow(defaultWorkflow)
                    .fromStatus(statuses.get(i))
                    .toStatus(statuses.get(i + 1))
                    .requiredPermission("TASK_UPDATE")
                    .build();
            stepRepository.save(step);
        }
    }
}