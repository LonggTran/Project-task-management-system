package com.projecttaskmanager.backend.events;

import com.projecttaskmanager.backend.models.enums.ActivityAction;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ActivityEvent {
    private ActivityAction action;
    private String entityType;
    private UUID entityId;
    private UUID projectId;
    private String metadata;
    private UUID actorId;
}