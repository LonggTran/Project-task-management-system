package com.projecttaskmanager.backend.config;

import com.projecttaskmanager.backend.models.ProjectRole;
import com.projecttaskmanager.backend.models.Permission;
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

        Permission read = createPermission("PROJECT_READ");
        Permission update = createPermission("PROJECT_UPDATE");
        Permission delete = createPermission("PROJECT_DELETE");
        Permission addMember = createPermission("MEMBER_ADD");
        Permission removeMember = createPermission("MEMBER_REMOVE");
        Permission updateMember = createPermission("MEMBER_UPDATE");

        createRole("OWNER", Set.of(read, update, delete, addMember, removeMember, updateMember));
        createRole("ADMIN_PROJECT", Set.of(read, update, addMember, updateMember));
        createRole("MEMBER", Set.of(read));
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