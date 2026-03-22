package com.projecttaskmanager.backend.events;

import com.projecttaskmanager.backend.models.enums.ActivityAction;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ActivityHelper {

    private final ApplicationEventPublisher publisher;

    public void log(ActivityAction action,
                    String entityType,
                    UUID entityId,
                    UUID projectId,
                    String metadata,
                    UUID actorId) {

        publisher.publishEvent(
                ActivityEvent.builder()
                        .action(action)
                        .entityType(entityType)
                        .entityId(entityId)
                        .projectId(projectId)
                        .metadata(metadata)
                        .actorId(actorId)
                        .build()
        );
    }
}
