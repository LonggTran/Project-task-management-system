package com.projecttaskmanager.backend.events;

import com.projecttaskmanager.backend.repositories.ProjectMemberRepository;
import com.projecttaskmanager.backend.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationService notificationService;
    private final ProjectMemberRepository projectMemberRepository;

    @Async
    @EventListener
    public void handle(NotificationEvent event) {
        if (event.getReceiverId() != null) {
            // Gửi đến người dùng cụ thể
            notificationService.send(
                    event.getReceiverId(),
                    event.getTitle(),
                    event.getContent(),
                    event.getType(),
                    event.getReferenceId(),
                    event.getActorId()
            );
        } else if (event.getProjectId() != null) {
            // Broadcast đến tất cả thành viên dự án
            projectMemberRepository.findByProjectId(event.getProjectId())
                    .stream()
                    .filter(member -> !member.getUser().getId().equals(event.getActorId())) // Không thông báo cho người thực hiện
                    .forEach(member -> {
                        notificationService.send(
                                member.getUser().getId(),
                                event.getTitle(),
                                event.getContent(),
                                event.getType(),
                                event.getReferenceId(),
                                event.getActorId()
                        );
                    });
        }
    }
}