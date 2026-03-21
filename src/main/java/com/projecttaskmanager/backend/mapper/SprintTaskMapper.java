package com.projecttaskmanager.backend.mapper;

import com.projecttaskmanager.backend.dto.response.sprint.SprintTaskResponse;
import com.projecttaskmanager.backend.models.SprintTask;
import org.springframework.stereotype.Component;

@Component
public class SprintTaskMapper {

    public SprintTaskResponse toResponse(SprintTask st) {
        return SprintTaskResponse.builder()
                .id(st.getId())
                .sprintId(st.getSprint().getId())
                .taskId(st.getTask().getId())
                .build();
    }
}