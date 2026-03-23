package com.projecttaskmanager.backend.events;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class NotificationEvent {
    private UUID projectId;
    private UUID receiverId;  // Người nhận cụ thể, null nếu gửi cho tất cả
    private String title;
    private String content;
    private String type;
    private UUID referenceId;
    private UUID actorId;  // Người thực hiện hành động
}