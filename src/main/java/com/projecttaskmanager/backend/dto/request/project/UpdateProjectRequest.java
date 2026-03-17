package com.projecttaskmanager.backend.dto.request.project;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProjectRequest {
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isArchived;
}