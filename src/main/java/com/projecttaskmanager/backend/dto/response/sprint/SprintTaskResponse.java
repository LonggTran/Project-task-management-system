package com.projecttaskmanager.backend.dto.response.sprint;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class SprintTaskResponse {
    private UUID id;
    private UUID sprintId;
    private UUID taskId;
}