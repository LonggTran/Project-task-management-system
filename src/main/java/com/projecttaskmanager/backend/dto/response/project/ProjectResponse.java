package com.projecttaskmanager.backend.dto.response.project;

import com.projecttaskmanager.backend.dto.response.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    private UUID id;
    private String name;
    private String description;
    private UserResponse owner;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isArchived;
}