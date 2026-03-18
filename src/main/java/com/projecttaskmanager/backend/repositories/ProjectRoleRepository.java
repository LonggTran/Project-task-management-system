package com.projecttaskmanager.backend.repositories;

import com.projecttaskmanager.backend.models.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRoleRepository extends JpaRepository<ProjectRole, UUID> {
    Optional<ProjectRole> findByName(String name);
}
