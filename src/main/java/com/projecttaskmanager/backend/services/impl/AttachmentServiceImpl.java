package com.projecttaskmanager.backend.services.impl;

import com.projecttaskmanager.backend.dto.response.attachment.AttachmentResponse;
import com.projecttaskmanager.backend.events.ActivityHelper;
import com.projecttaskmanager.backend.exceptions.AppException;
import com.projecttaskmanager.backend.exceptions.ErrorCode;
import com.projecttaskmanager.backend.mapper.AttachmentMapper;
import com.projecttaskmanager.backend.models.Attachment;
import com.projecttaskmanager.backend.models.Task;
import com.projecttaskmanager.backend.models.User;
import com.projecttaskmanager.backend.models.enums.ActivityAction;
import com.projecttaskmanager.backend.repositories.AttachmentRepository;
import com.projecttaskmanager.backend.repositories.TaskRepository;
import com.projecttaskmanager.backend.repositories.UserRepository;
import com.projecttaskmanager.backend.services.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final AttachmentMapper attachmentMapper;
    private final ActivityHelper activityHelper;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public AttachmentResponse upload(UUID taskId, MultipartFile file) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("Đã tạo thư mục upload: " + uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String sanitizedFilename = originalFilename != null ?
                    originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_") : "unknown";

            String fileName = UUID.randomUUID() + "_" + sanitizedFilename;

            Path filePath = uploadPath.resolve(fileName);

            file.transferTo(filePath.toFile());
            System.out.println("Đã lưu file tại: " + filePath);

            String relativePath = uploadDir + "/" + fileName;

            User user = userRepository.findByEmail(
                    SecurityContextHolder.getContext().getAuthentication().getName()
            ).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            Attachment attachment = Attachment.builder()
                    .task(task)
                    .fileName(originalFilename)
                    .fileUrl(relativePath)
                    .uploadedBy(user)
                    .uploadedAt(Instant.now())
                    .build();

            Attachment saved = attachmentRepository.save(attachment);

            activityHelper.log(
                    ActivityAction.ATTACHMENT_UPLOADED,
                    "ATTACHMENT",
                    saved.getId(),
                    task.getProject().getId(),
                    "Uploaded file: " + saved.getFileName(),
                    user.getId()
            );

            return attachmentMapper.toResponse(saved);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Không thể upload file: " + e.getMessage(), e);
        }
    }

    @Override
    public List<AttachmentResponse> getByTask(UUID taskId) {
        return attachmentRepository.findByTaskId(taskId)
                .stream()
                .map(attachmentMapper::toResponse)
                .toList();
    }

    @Override
    public void delete(UUID id) {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        try {
            Path filePath = Paths.get(attachment.getFileUrl()).toAbsolutePath().normalize();
            java.io.File file = filePath.toFile();

            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    System.out.println("Đã xóa file: " + filePath);
                } else {
                    System.err.println("Không thể xóa file: " + filePath);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa file: " + e.getMessage());
        }

        attachmentRepository.delete(attachment);

        activityHelper.log(
                ActivityAction.ATTACHMENT_DELETED,
                "ATTACHMENT",
                attachment.getId(),
                attachment.getTask().getProject().getId(),
                "Deleted file: " + attachment.getFileName(),
                attachment.getUploadedBy().getId()
        );
    }
}