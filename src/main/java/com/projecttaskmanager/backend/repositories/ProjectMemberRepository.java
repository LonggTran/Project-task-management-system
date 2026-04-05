package com.projecttaskmanager.backend.repositories;

import com.projecttaskmanager.backend.models.Project;
import com.projecttaskmanager.backend.models.ProjectMember;
import com.projecttaskmanager.backend.models.User;
import com.projecttaskmanager.backend.models.baseModels.ProjectMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {
    List<ProjectMember> findByProject(Project project);
    List<ProjectMember> findByUser(User user);
    Optional<ProjectMember> findByProjectAndUser(Project project, User user);
    void deleteByProject(Project project);
    List<ProjectMember> findByProjectId(UUID id);
    boolean existsByProjectIdAndUserId(UUID projectId, UUID userId);
    Optional<ProjectMember> findByProjectIdAndUserId(UUID projectId, UUID userId);
}