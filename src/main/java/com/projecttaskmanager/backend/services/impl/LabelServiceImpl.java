// services/impl/LabelServiceImpl.java
package com.projecttaskmanager.backend.services.impl;

import com.projecttaskmanager.backend.dto.request.label.CreateLabelRequest;
import com.projecttaskmanager.backend.dto.request.label.UpdateLabelRequest;
import com.projecttaskmanager.backend.dto.response.label.LabelResponse;
import com.projecttaskmanager.backend.exceptions.AppException;
import com.projecttaskmanager.backend.exceptions.ErrorCode;
import com.projecttaskmanager.backend.mapper.LabelMapper;
import com.projecttaskmanager.backend.models.Label;
import com.projecttaskmanager.backend.models.Project;
import com.projecttaskmanager.backend.repositories.LabelRepository;
import com.projecttaskmanager.backend.repositories.ProjectRepository;
import com.projecttaskmanager.backend.services.LabelService;
import com.projecttaskmanager.backend.services.ProjectAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LabelServiceImpl implements LabelService {

    private final LabelRepository labelRepository;
    private final ProjectRepository projectRepository;
    private final LabelMapper labelMapper;
    private final ProjectAuthorizationService authorizationService;

    @Override
    public LabelResponse createLabel(CreateLabelRequest request, UUID projectId) {
        authorizationService.checkPermission(projectId, "TASK_LABEL");

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        if (labelRepository.existsByNameAndProject(request.getName(), project)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }

        Label label = Label.builder()
                .name(request.getName())
                .project(project)
                .color(request.getColor())
                .build();

        return labelMapper.toResponse(labelRepository.save(label));
    }

    @Override
    public LabelResponse updateLabel(UUID labelId, UpdateLabelRequest request, UUID projectId) {
        authorizationService.checkPermission(projectId, "TASK_LABEL");

        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR));

        if (request.getName() != null) {
            if (!request.getName().equals(label.getName())) {
                if (labelRepository.existsByNameAndProject(request.getName(), label.getProject())) {
                    throw new AppException(ErrorCode.VALIDATION_ERROR);
                }
                label.setName(request.getName());
            }
        }
        if (request.getColor() != null) label.setColor(request.getColor());

        return labelMapper.toResponse(labelRepository.save(label));
    }

    @Override
    public void deleteLabel(UUID labelId, UUID projectId) {
        authorizationService.checkPermission(projectId, "TASK_LABEL");

        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR));

        labelRepository.delete(label);
    }

    @Override
    public List<LabelResponse> getAllLabels(UUID projectId) {
        authorizationService.checkProjectMember(projectId);

        return labelRepository.findByProjectId(projectId)
                .stream()
                .map(labelMapper::toResponse)
                .toList();
    }

    @Override
    public LabelResponse getLabelById(UUID labelId, UUID projectId) {
        authorizationService.checkProjectMember(projectId);

        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR));

        return labelMapper.toResponse(label);
    }
}