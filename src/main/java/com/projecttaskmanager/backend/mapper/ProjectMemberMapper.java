package com.projecttaskmanager.backend.mapper;

import com.projecttaskmanager.backend.dto.response.project.ProjectMemberResponse;
import com.projecttaskmanager.backend.models.ProjectMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectMemberMapper {

    private final UserMapper userMapper;

    public ProjectMemberResponse toResponse(ProjectMember member) {
        return ProjectMemberResponse.builder()
                .projectId(member.getId().getProjectId())
                .user(userMapper.toResponse(member.getUser()))
                .projectRole(member.getProjectRole().getName())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}