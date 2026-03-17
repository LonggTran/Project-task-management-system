package com.projecttaskmanager.backend.dto.request.project;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateProjectRequest {
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
}