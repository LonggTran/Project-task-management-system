package com.projecttaskmanager.backend.dto.request.attachment;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateAttachmentRequest {
    @NotBlank(message = "File name is required")
    private String fileName;

    @NotBlank(message = "File URL is required")
    private String fileUrl;
}