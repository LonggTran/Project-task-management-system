package com.projecttaskmanager.backend.mapper;

import com.projecttaskmanager.backend.dto.response.attachment.AttachmentResponse;
import com.projecttaskmanager.backend.models.Attachment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttachmentMapper {

    private final UserMapper userMapper;

    public AttachmentResponse toResponse(Attachment attachment) {
        if (attachment == null) return null;

        return AttachmentResponse.builder()
                .id(attachment.getId())
                .fileName(attachment.getFileName())
                .fileUrl(attachment.getFileUrl())
                .taskId(attachment.getTask().getId())
                .uploadedBy(userMapper.toResponse(attachment.getUploadedBy()))
                .uploadedAt(attachment.getUploadedAt())
                .build();
    }
}