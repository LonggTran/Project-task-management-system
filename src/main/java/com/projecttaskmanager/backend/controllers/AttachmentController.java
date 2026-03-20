package com.projecttaskmanager.backend.controllers;

import com.projecttaskmanager.backend.dto.response.ApiResponse;
import com.projecttaskmanager.backend.dto.response.attachment.AttachmentResponse;
import com.projecttaskmanager.backend.services.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping("/upload/{taskId}")
    public ResponseEntity<ApiResponse<AttachmentResponse>> upload(
            @PathVariable UUID taskId,
            @RequestParam("file") MultipartFile file
    ) {
        AttachmentResponse response = attachmentService.upload(taskId, file);

        return ResponseEntity.ok(ApiResponse.<AttachmentResponse>builder()
                .success(true)
                .data(response)
                .message("upload success")
                .timestamp(Instant.now())
                .build());
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> getByTask(@PathVariable UUID taskId) {

        return ResponseEntity.ok(ApiResponse.<List<AttachmentResponse>>builder()
                .success(true)
                .data(attachmentService.getByTask(taskId))
                .timestamp(Instant.now())
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {

        attachmentService.delete(id);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Deleted")
                .timestamp(Instant.now())
                .build());
    }
}