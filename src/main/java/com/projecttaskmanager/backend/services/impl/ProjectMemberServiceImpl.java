package com.projecttaskmanager.backend.services.impl;

import com.projecttaskmanager.backend.dto.request.project.AddMemberRequest;
import com.projecttaskmanager.backend.dto.response.project.ProjectMemberResponse;
import com.projecttaskmanager.backend.exceptions.AppException;
import com.projecttaskmanager.backend.exceptions.ErrorCode;
import com.projecttaskmanager.backend.mapper.ProjectMemberMapper;
import com.projecttaskmanager.backend.models.*;
import com.projecttaskmanager.backend.models.baseModels.ProjectMemberId;
import com.projecttaskmanager.backend.repositories.*;
import com.projecttaskmanager.backend.services.EmailService;
import com.projecttaskmanager.backend.services.ProjectAuthorizationService;
import com.projecttaskmanager.backend.services.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.projecttaskmanager.backend.events.NotificationEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final ProjectAuthorizationService authorizationService;
    private final ProjectMemberMapper projectMemberMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final ProjectInvitationRepository invitationRepository;
    private final EmailService emailService;

    private User getCurrentUser() {
        return userRepository.findByEmail(
                SecurityContextHolder.getContext().getAuthentication().getName()
        ).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public ProjectMemberResponse addMember(UUID projectId, AddMemberRequest request) {
        authorizationService.checkPermission(projectId, "MEMBER_ADD");

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        ProjectRole role = projectRoleRepository.findByName(request.getProjectRole())
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR));

        User currentUser = getCurrentUser();

        boolean exists = projectMemberRepository
                .findByProjectAndUser(project, user)
                .isPresent();

        if (exists) {
            throw new AppException(ErrorCode.USER_ALREADY_IN_PROJECT);
        }

        ProjectMember member = ProjectMember.builder()
                .id(new ProjectMemberId(projectId, user.getId()))
                .project(project)
                .user(user)
                .projectRole(role)
                .joinedAt(Instant.now())
                .build();

        ProjectMember saved = projectMemberRepository.save(member);

        eventPublisher.publishEvent(
                NotificationEvent.builder()
                        .receiverId(user.getId())
                        .projectId(project.getId())
                        .title("Được thêm vào dự án")
                        .content(String.format("Bạn đã được %s thêm vào dự án '%s'",
                                currentUser.getFullName(), project.getName()))
                        .type("PROJECT_MEMBER_ADDED")
                        .referenceId(project.getId())
                        .actorId(currentUser.getId())
                        .build()
        );

        return projectMemberMapper.toResponse(saved);
    }

    @Override
    public List<ProjectMemberResponse> getMembers(UUID projectId) {

        authorizationService.checkProjectMember(projectId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        return projectMemberRepository.findByProject(project)
                .stream()
                .map(projectMemberMapper::toResponse)
                .toList();
    }

    @Override
    public void removeMember(UUID projectId, UUID userId) {

        authorizationService.checkPermission(projectId, "MEMBER_REMOVE");

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        ProjectMember member = projectMemberRepository
                .findByProjectAndUser(project, user)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR));

        if (member.getProjectRole().getName().equals("OWNER")) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        projectMemberRepository.delete(member);
    }

    @Override
    public void updateMemberRole(UUID projectId, UUID userId, String roleName) {

        authorizationService.checkPermission(projectId, "MEMBER_ADD");

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        ProjectMember member = projectMemberRepository
                .findByProjectAndUser(project, user)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR));

        if (member.getProjectRole().getName().equals("OWNER")) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        ProjectRole newRole = projectRoleRepository.findByName(roleName)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR));

        member.setProjectRole(newRole);

        projectMemberRepository.save(member);
    }

    @Override
    public void inviteMember(UUID projectId, AddMemberRequest request) {

        authorizationService.checkPermission(projectId, "MEMBER_ADD");

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        ProjectRole role = projectRoleRepository.findByName(request.getProjectRole())
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR));

        User currentUser = getCurrentUser();

        String token = UUID.randomUUID().toString();

        ProjectInvitation invitation = ProjectInvitation.builder()
                .email(request.getEmail())
                .project(project)
                .role(role)
                .token(token)
                .expiredAt(Instant.now().plusSeconds(86400)) // 24h
                .accepted(false)
                .build();

        invitationRepository.save(invitation);

        String link = "http://localhost:5173/invitations/accept?token=" + token;

        emailService.send(
                request.getEmail(),
                "Project Invitation",
                String.format("""
                    Bạn được %s mời vào project "%s"

                    Click để tham gia:
                    %s
                    """,
                        currentUser.getFullName(),
                        project.getName(),
                        link
                )
        );
    }

    @Override
    public void acceptInvitation(String token) {

        ProjectInvitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_ALREADY_EXISTS));

        if (invitation.isAccepted()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }

        if (invitation.getExpiredAt().isBefore(Instant.now())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = getCurrentUser();

        if (!user.getEmail().equals(invitation.getEmail())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        Project project = invitation.getProject();

        boolean exists = projectMemberRepository
                .findByProjectAndUser(project, user)
                .isPresent();

        if (exists) {
            throw new AppException(ErrorCode.USER_ALREADY_IN_PROJECT);
        }

        ProjectMember member = ProjectMember.builder()
                .id(new ProjectMemberId(project.getId(), user.getId()))
                .project(project)
                .user(user)
                .projectRole(invitation.getRole())
                .joinedAt(Instant.now())
                .build();

        projectMemberRepository.save(member);

        invitation.setAccepted(true);
        invitationRepository.save(invitation);
    }
}