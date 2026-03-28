package com.projecttaskmanager.backend.controllers;

import com.projecttaskmanager.backend.dto.response.ApiResponse;
import com.projecttaskmanager.backend.dto.response.attachment.AttachmentResponse;
import com.projecttaskmanager.backend.services.AttachmentService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            @RequestParam("file") @NotNull(message = "File is required") MultipartFile file
    ) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.<AttachmentResponse>builder()
                    .success(false)
                    .message("File is empty")
                    .timestamp(Instant.now())
                    .build());
        }

        AttachmentResponse response = attachmentService.upload(taskId, file);
        return ResponseEntity.ok(ApiResponse.<AttachmentResponse>builder()
                .success(true)
                .data(response)
                .message("Upload success")
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

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam String path) {
        try {
            String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8.name());
            Path filePath = Paths.get(decodedPath).toAbsolutePath().normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = "application/octet-stream";
                String fileName = filePath.getFileName().toString();
                String originalFileName = fileName.substring(fileName.indexOf('_') + 1);

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + originalFileName + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}