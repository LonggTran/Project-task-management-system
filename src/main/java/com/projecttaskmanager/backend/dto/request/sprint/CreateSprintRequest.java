package com.projecttaskmanager.backend.dto.request.sprint;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateSprintRequest {
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private UUID projectId;
}