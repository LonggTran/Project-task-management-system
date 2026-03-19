package com.projecttaskmanager.backend.mapper;

import com.projecttaskmanager.backend.dto.response.project.ProjectResponse;
import com.projecttaskmanager.backend.models.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectMapper {

    private final UserMapper userMapper;

    public ProjectResponse toResponse(Project project) {
        if (project == null) return null;

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .owner(userMapper.toResponse(project.getOwner()))
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .isArchived(project.getIsArchived())
                .build();
    }
}