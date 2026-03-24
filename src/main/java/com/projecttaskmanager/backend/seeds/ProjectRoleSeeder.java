package com.projecttaskmanager.backend.seeds;

import com.projecttaskmanager.backend.models.Permission;
import com.projecttaskmanager.backend.models.ProjectRole;
import com.projecttaskmanager.backend.repositories.PermissionRepository;
import com.projecttaskmanager.backend.repositories.ProjectRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
@Transactional
public class ProjectRoleSeeder implements CommandLineRunner {

    private final ProjectRoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public void run(String... args) {

        // =======================
        // PROJECT PERMISSIONS
        // =======================
        Permission projectRead = createPermission("PROJECT_READ");
        Permission projectCreate = createPermission("PROJECT_CREATE");
        Permission projectUpdate = createPermission("PROJECT_UPDATE");
        Permission projectDelete = createPermission("PROJECT_DELETE");
        Permission projectArchive = createPermission("PROJECT_ARCHIVE");
        Permission activityView = createPermission("ACTIVITY_VIEW");

        // =======================
        // MEMBER PERMISSIONS
        // =======================
        Permission memberAdd = createPermission("MEMBER_ADD");
        Permission memberRemove = createPermission("MEMBER_REMOVE");
        Permission memberUpdate = createPermission("MEMBER_UPDATE");
        Permission memberView = createPermission("MEMBER_VIEW");

        // =======================
        // TASK PERMISSIONS
        // =======================
        Permission taskCreate = createPermission("TASK_CREATE");
        Permission taskUpdate = createPermission("TASK_UPDATE");
        Permission taskDelete = createPermission("TASK_DELETE");
        Permission taskView = createPermission("TASK_VIEW");
        Permission taskAssign = createPermission("TASK_ASSIGN");
        Permission taskComment = createPermission("TASK_COMMENT");
        Permission taskLabel = createPermission("TASK_LABEL");
        Permission taskDependency = createPermission("TASK_DEPENDENCY");
        Permission taskPriority = createPermission("TASK_PRIORITY");
        Permission taskStatus = createPermission("TASK_STATUS");

        // =======================
        // EPIC PERMISSIONS
        // =======================
        Permission epicCreate = createPermission("EPIC_CREATE");
        Permission epicUpdate = createPermission("EPIC_UPDATE");
        Permission epicDelete = createPermission("EPIC_DELETE");
        Permission epicView = createPermission("EPIC_VIEW");

        // =======================
        // SPRINT PERMISSIONS
        // =======================
        Permission sprintCreate = createPermission("SPRINT_CREATE");
        Permission sprintUpdate = createPermission("SPRINT_UPDATE");
        Permission sprintDelete = createPermission("SPRINT_DELETE");
        Permission sprintView = createPermission("SPRINT_VIEW");

        // =======================
        // ATTACHMENT PERMISSIONS
        // =======================
        Permission attachmentUpload = createPermission("ATTACHMENT_UPLOAD");
        Permission attachmentDelete = createPermission("ATTACHMENT_DELETE");
        Permission attachmentView = createPermission("ATTACHMENT_VIEW");

        // =======================
        // NOTIFICATION PERMISSIONS
        // =======================
        Permission notificationSend = createPermission("NOTIFICATION_SEND");
        Permission notificationView = createPermission("NOTIFICATION_VIEW");

        // =======================
        // COMMENT PERMISSIONS
        // =======================

        Permission commentCreate = createPermission("COMMENT_CREATE");
        Permission commentUpdate = createPermission("COMMENT_UPDATE");
        Permission commentDelete = createPermission("COMMENT_DELETE");
        Permission commentView = createPermission("COMMENT_VIEW");

        // =======================
        // CREATE ROLES
        // =======================

        // OWNER → full permissions
        createRole("OWNER", Set.of(
                projectRead, projectCreate, projectUpdate, projectDelete, projectArchive,
                memberAdd, memberRemove, memberUpdate, memberView,
                taskCreate, taskUpdate, taskDelete, taskView, taskAssign, taskComment, taskLabel, taskDependency, taskPriority, taskStatus,
                epicCreate, epicUpdate, epicDelete, epicView,
                sprintCreate, sprintUpdate, sprintDelete, sprintView,
                attachmentUpload, attachmentDelete, attachmentView,
                notificationSend, notificationView,
                commentCreate, commentUpdate, commentDelete, commentView, activityView
        ));

        // ADMIN_PROJECT → can manage project & tasks but not delete project
        createRole("ADMIN_PROJECT", Set.of(
                projectRead, projectUpdate,
                memberAdd, memberUpdate, memberView,
                taskCreate, taskUpdate, taskDelete, taskView, taskAssign, taskComment, taskLabel, taskDependency, taskPriority, taskStatus,
                epicCreate, epicUpdate, epicDelete, epicView,
                sprintCreate, sprintUpdate, sprintDelete, sprintView,
                attachmentUpload, attachmentDelete, attachmentView,
                notificationSend, notificationView,
                commentCreate, commentUpdate, commentDelete, commentView, activityView
        ));

        // MEMBER → basic access
        createRole("MEMBER", Set.of(
                projectRead,
                memberView,
                taskView, taskComment, taskLabel,
                epicView,
                sprintView,
                attachmentUpload, attachmentView,
                notificationView,
                commentCreate, commentView, activityView
        ));
    }

    private Permission createPermission(String name) {
        return permissionRepository.findByName(name)
                .orElseGet(() -> permissionRepository.save(
                        Permission.builder()
                                .name(name)
                                .build()
                ));
    }

    private void createRole(String name, Set<Permission> permissions) {
        if (roleRepository.findByName(name).isEmpty()) {
            roleRepository.save(
                    ProjectRole.builder()
                            .name(name)
                            .permissions(permissions)
                            .build()
            );
        }
    }
}