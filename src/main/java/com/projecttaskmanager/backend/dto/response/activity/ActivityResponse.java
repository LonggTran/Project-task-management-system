package com.projecttaskmanager.backend.dto.response.activity;

import com.projecttaskmanager.backend.models.enums.ActivityAction;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ActivityResponse {
    private UUID id;
    private ActivityAction action;
    private String entityType;
    private UUID entityId;
    private UUID projectId;
    private String actorName;
    private Instant createdAt;
    private String metadata;
}