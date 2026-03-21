package com.projecttaskmanager.backend.mapper;

import com.projecttaskmanager.backend.dto.response.epic.EpicResponse;
import com.projecttaskmanager.backend.models.Epic;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EpicMapper {

    private final UserMapper userMapper;

    public EpicResponse toResponse(Epic epic) {
        if (epic == null) return null;

        return EpicResponse.builder()
                .id(epic.getId())
                .name(epic.getName())
                .description(epic.getDescription())
                .projectId(epic.getProject().getId())
                .createdBy(userMapper.toResponse(epic.getCreatedBy()))
                .startDate(epic.getStartDate())
                .endDate(epic.getEndDate())
                .build();
    }
}