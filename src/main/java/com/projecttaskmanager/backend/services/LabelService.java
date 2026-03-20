package com.projecttaskmanager.backend.services;

import com.projecttaskmanager.backend.dto.request.label.CreateLabelRequest;
import com.projecttaskmanager.backend.dto.request.label.UpdateLabelRequest;
import com.projecttaskmanager.backend.dto.response.label.LabelResponse;

import java.util.List;
import java.util.UUID;

public interface LabelService {

    LabelResponse createLabel(CreateLabelRequest request, UUID projectId);

    LabelResponse updateLabel(UUID labelId, UpdateLabelRequest request, UUID projectId);

    void deleteLabel(UUID labelId, UUID projectId);

    List<LabelResponse> getAllLabels(UUID projectId);

    LabelResponse getLabelById(UUID labelId, UUID projectId);
}