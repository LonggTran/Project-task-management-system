// seeds/TaskStatusSeederService.java
package com.projecttaskmanager.backend.seeds;

import com.projecttaskmanager.backend.models.Project;
import com.projecttaskmanager.backend.models.TaskStatus;
import com.projecttaskmanager.backend.models.enums.TaskStatusCategory;
import com.projecttaskmanager.backend.repositories.TaskStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskStatusSeederService {

    private final TaskStatusRepository taskStatusRepository;

    public void seedDefaultStatuses(Project project) {

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
    }
}