package com.projecttaskmanager.backend.repositories;

import com.projecttaskmanager.backend.models.Project;
import com.projecttaskmanager.backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findByOwner(User owner);
}