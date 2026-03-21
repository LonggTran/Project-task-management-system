package com.projecttaskmanager.backend.mapper;

import com.projecttaskmanager.backend.dto.response.sprint.SprintResponse;
import com.projecttaskmanager.backend.models.Sprint;
import org.springframework.stereotype.Component;

@Component
public class SprintMapper {

    public SprintResponse toResponse(Sprint sprint) {
        return SprintResponse.builder()
                .id(sprint.getId())
                .name(sprint.getName())
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .status(sprint.getStatus())
                .projectId(sprint.getProject().getId())
                .build();
    }
}