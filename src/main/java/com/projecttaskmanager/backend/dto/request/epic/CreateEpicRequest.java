package com.projecttaskmanager.backend.dto.request.epic;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateEpicRequest {
    private String name;
    private String description;
    private UUID projectId;
    private LocalDate startDate;
    private LocalDate endDate;
}