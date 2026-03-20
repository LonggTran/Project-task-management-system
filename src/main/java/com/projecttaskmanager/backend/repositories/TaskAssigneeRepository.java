package com.projecttaskmanager.backend.repositories;

import com.projecttaskmanager.backend.models.Task;
import com.projecttaskmanager.backend.models.TaskAssignee;
import com.projecttaskmanager.backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskAssigneeRepository extends JpaRepository<TaskAssignee, UUID> {
    List<TaskAssignee> findByTask(Task task);
    Optional<TaskAssignee> findByTaskAndUser(Task task, User user);
    void deleteByTask(Task task);
}