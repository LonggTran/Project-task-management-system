package com.projecttaskmanager.backend.repositories;

import com.projecttaskmanager.backend.models.Sprint;
import com.projecttaskmanager.backend.models.SprintTask;
import com.projecttaskmanager.backend.models.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SprintTaskRepository extends JpaRepository<SprintTask, UUID> {
    List<SprintTask> findBySprint(Sprint sprint);
    Optional<SprintTask> findBySprintAndTask(Sprint sprint, Task task);
}