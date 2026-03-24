// seeds/WorkflowSeederService.java
package com.projecttaskmanager.backend.seeds;

import com.projecttaskmanager.backend.models.Project;
import com.projecttaskmanager.backend.models.TaskStatus;
import com.projecttaskmanager.backend.models.Workflow;
import com.projecttaskmanager.backend.models.WorkflowStep;
import com.projecttaskmanager.backend.repositories.TaskStatusRepository;
import com.projecttaskmanager.backend.repositories.WorkflowRepository;
import com.projecttaskmanager.backend.repositories.WorkflowStepRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkflowSeederService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowStepRepository workflowStepRepository;
    private final TaskStatusRepository taskStatusRepository;

    public void seedDefaultWorkflow(Project project) {
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
            workflowStepRepository.save(step);
        }
    }

    public boolean hasWorkflow(Project project) {
        return workflowRepository.findByProject(project).isPresent();
    }
}