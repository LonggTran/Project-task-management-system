package com.projecttaskmanager.backend.events;

import com.projecttaskmanager.backend.services.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivityListener {

    private final ActivityLogService activityLogService;

    @Async
    @EventListener
    public void handle(ActivityEvent event) {
        activityLogService.log(
                event.getAction(),
                event.getEntityType(),
                event.getEntityId(),
                event.getProjectId(),
                event.getMetadata(),
                event.getActorId()
        );
    }
}