package com.projecttaskmanager.backend.dto.request.attachment;

import lombok.Data;

@Data
public class CreateAttachmentRequest {
    private String fileName;
    private String fileUrl;
}
