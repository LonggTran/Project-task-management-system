package com.projecttaskmanager.backend.repositories;

import com.projecttaskmanager.backend.models.Project;
import com.projecttaskmanager.backend.models.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SprintRepository extends JpaRepository<Sprint, UUID> {
    List<Sprint> findByProject(Project project);
}