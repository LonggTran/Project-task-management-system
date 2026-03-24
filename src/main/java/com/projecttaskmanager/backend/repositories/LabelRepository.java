package com.projecttaskmanager.backend.repositories;

import com.projecttaskmanager.backend.models.Label;
import com.projecttaskmanager.backend.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LabelRepository extends JpaRepository<Label, UUID> {
    List<Label> findByProjectId(UUID projectId);
    boolean existsByNameAndProject(String name, Project project);
    Optional<Label> findByNameAndProject(String name, Project project);
}