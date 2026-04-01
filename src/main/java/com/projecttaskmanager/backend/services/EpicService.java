package com.projecttaskmanager.backend.services;

import com.projecttaskmanager.backend.dto.request.epic.CreateEpicRequest;
import com.projecttaskmanager.backend.dto.request.epic.UpdateEpicRequest;
import com.projecttaskmanager.backend.dto.response.epic.EpicResponse;

import java.util.List;
import java.util.UUID;

public interface EpicService {
    EpicResponse create(CreateEpicRequest request, String email);
    EpicResponse update(UUID epicId, UpdateEpicRequest request, String email);
    void delete(UUID epicId, String email);
    EpicResponse getById(UUID epicId);
    List<EpicResponse> getAllByProject(UUID projectId);
}