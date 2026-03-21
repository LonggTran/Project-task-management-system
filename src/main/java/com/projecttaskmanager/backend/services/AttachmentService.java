package com.projecttaskmanager.backend.services;

import com.projecttaskmanager.backend.dto.response.attachment.AttachmentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface AttachmentService {
    AttachmentResponse upload(UUID taskId, MultipartFile file);
    List<AttachmentResponse> getByTask(UUID taskId);
    void delete(UUID id);
}