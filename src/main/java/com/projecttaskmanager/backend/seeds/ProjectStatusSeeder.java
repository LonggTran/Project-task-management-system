// config/ProjectStatusSeeder.java
package com.projecttaskmanager.backend.seeds;

import com.projecttaskmanager.backend.models.Project;
import com.projecttaskmanager.backend.models.TaskStatus;
import com.projecttaskmanager.backend.models.enums.TaskStatusCategory;
import com.projecttaskmanager.backend.repositories.TaskStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectStatusSeeder {

    private final TaskStatusRepository taskStatusRepository;

    @Transactional
    public void seedDefaultStatusesForProject(Project project) {
        log.info("Seeding default statuses for project: {}", project.getName());

        List<TaskStatus> defaultStatuses = Arrays.asList(
                TaskStatus.builder()
                        .name("To Do")
                        .category(TaskStatusCategory.TODO)
                        .isDefault(true)
                        .sort(1)
                        .project(project)
                        .build(),
                TaskStatus.builder()
                        .name("In Progress")
                        .category(TaskStatusCategory.IN_PROGRESS)
                        .isDefault(false)
                        .sort(2)
                        .project(project)
                        .build(),
                TaskStatus.builder()
                        .name("Testing")
                        .category(TaskStatusCategory.TEST)
                        .isDefault(false)
                        .sort(3)
                        .project(project)
                        .build(),
                TaskStatus.builder()
                        .name("Done")
                        .category(TaskStatusCategory.DONE)
                        .isDefault(false)
                        .sort(4)
                        .project(project)
                        .build()
        );

        taskStatusRepository.saveAll(defaultStatuses);
        log.info("Created {} default statuses for project: {}", defaultStatuses.size(), project.getName());
    }
}