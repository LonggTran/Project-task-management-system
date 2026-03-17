package com.projecttaskmanager.backend.dto.response.project;

import com.projecttaskmanager.backend.dto.response.UserResponse;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class ProjectResponse {
    private UUID id;

    private String name;

    private String description;

    private UserResponse owner;

    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean isArchived;
}