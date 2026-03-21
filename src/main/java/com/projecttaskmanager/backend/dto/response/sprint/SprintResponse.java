package com.projecttaskmanager.backend.dto.response.sprint;

import com.projecttaskmanager.backend.models.enums.SprintStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class SprintResponse {
    private UUID id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private SprintStatus status;
    private UUID projectId;
}