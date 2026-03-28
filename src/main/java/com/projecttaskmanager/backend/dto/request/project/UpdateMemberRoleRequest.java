package com.projecttaskmanager.backend.dto.request.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateMemberRoleRequest {
    @NotBlank(message = "Project role is required")
    @Pattern(regexp = "^(OWNER|ADMIN_PROJECT|MEMBER)$", message = "Project role must be ADMIN_PROJECT, MEMBER, or OWNER")
    private String projectRole;
}