package com.projecttaskmanager.backend.services;

import java.util.UUID;

public interface ProjectAuthorizationService {
    void checkProjectMember(UUID projectId);
    void checkPermission(UUID projectId, String permission);
    void checkAnyPermission(UUID projectId, String... permissions);
}