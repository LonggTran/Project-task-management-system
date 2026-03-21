package com.projecttaskmanager.backend.dto.response.epic;

import com.projecttaskmanager.backend.dto.response.UserResponse;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class EpicResponse {
    private UUID id;
    private String name;
    private String description;
    private UUID projectId;
    private UserResponse createdBy;
    private LocalDate startDate;
    private LocalDate endDate;
}