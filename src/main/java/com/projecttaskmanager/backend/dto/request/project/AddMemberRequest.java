package com.projecttaskmanager.backend.dto.request.project;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.UUID;

@Data
public class AddMemberRequest {
    private UUID userId;

    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^(OWNER|ADMIN_PROJECT|MEMBER)$", message = "Project role must be ADMIN_PROJECT, MEMBER, or OWNER")
    private String projectRole;
}