package com.projecttaskmanager.backend.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.projecttaskmanager.backend.constants.CacheKey;
import com.projecttaskmanager.backend.dto.request.project.AddMemberRequest;
import com.projecttaskmanager.backend.dto.response.project.ProjectMemberResponse;
import com.projecttaskmanager.backend.exceptions.AppException;
import com.projecttaskmanager.backend.exceptions.ErrorCode;
import com.projecttaskmanager.backend.helpers.EntityHelper;
import com.projecttaskmanager.backend.mapper.ProjectMemberMapper;
import com.projecttaskmanager.backend.models.*;
import com.projecttaskmanager.backend.models.baseModels.ProjectMemberId;
import com.projecttaskmanager.backend.repositories.*;
import com.projecttaskmanager.backend.services.EmailService;
import com.projecttaskmanager.backend.services.ProjectAuthorizationService;
import com.projecttaskmanager.backend.services.ProjectMemberService;
import com.projecttaskmanager.backend.services.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.projecttaskmanager.backend.events.NotificationEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final ProjectAuthorizationService authorizationService;
    private final ProjectMemberMapper projectMemberMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final ProjectInvitationRepository invitationRepository;
    private final EmailService emailService;
    private final RedisService redisService;
    private final EntityHelper entityHelper;

    private User getCurrentUser() {
        return entityHelper.getUserOrThrow(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );
    }

    @Override
    public ProjectMemberResponse addMember(UUID projectId, AddMemberRequest request) {

        authorizationService.checkPermission(projectId, "MEMBER_ADD");

        Project project = entityHelper.getProjectOrThrow(projectId);
        User user = entityHelper.getUserOrThrowById(request.getUserId());
        ProjectRole role = getRoleOrThrow(request.getProjectRole());

        User currentUser = getCurrentUser();

        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, user.getId())) {
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

        invalidateMemberCache(projectId);

        publishAddMemberEvent(project, user, currentUser);

        return projectMemberMapper.toResponse(saved);
    }

    @Override
    public List<ProjectMemberResponse> getMembers(UUID projectId) {

        authorizationService.checkProjectMember(projectId);

        String cacheKey = CacheKey.projectMembers(projectId);

        return redisService.getOrLoad(
                cacheKey,
                new TypeReference<List<ProjectMemberResponse>>() {},
                () -> {
                    Project project = entityHelper.getProjectOrThrow(projectId);

                    return projectMemberRepository.findByProject(project)
                            .stream()
                            .map(projectMemberMapper::toResponse)
                            .toList();
                },
                10, TimeUnit.MINUTES
        );
    }

    @Override
    public void removeMember(UUID projectId, UUID userId) {

        authorizationService.checkPermission(projectId, "MEMBER_REMOVE");

        ProjectMember member = projectMemberRepository
                .findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR));

        if (isOwner(member)) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        projectMemberRepository.delete(member);

        invalidateMemberCache(projectId);
    }

    @Override
    public void updateMemberRole(UUID projectId, UUID userId, String roleName) {

        authorizationService.checkPermission(projectId, "MEMBER_ADD");

        ProjectMember member = projectMemberRepository
                .findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR));

        if (isOwner(member)) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        ProjectRole newRole = getRoleOrThrow(roleName);

        member.setProjectRole(newRole);

        projectMemberRepository.save(member);

        invalidateMemberCache(projectId);
    }

    @Override
    public void inviteMember(UUID projectId, AddMemberRequest request) {

        authorizationService.checkPermission(projectId, "MEMBER_ADD");

        Project project = entityHelper.getProjectOrThrow(projectId);
        ProjectRole role = getRoleOrThrow(request.getProjectRole());
        User currentUser = getCurrentUser();

        String token = UUID.randomUUID().toString();

        ProjectInvitation invitation = ProjectInvitation.builder()
                .email(request.getEmail())
                .project(project)
                .role(role)
                .token(token)
                .expiredAt(Instant.now().plusSeconds(86400))
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
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR));

        if (invitation.isAccepted()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }

        if (invitation.getExpiredAt().isBefore(Instant.now())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }

        User user = getCurrentUser();

        if (!user.getEmail().equals(invitation.getEmail())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        Project project = invitation.getProject();

        if (projectMemberRepository.existsByProjectIdAndUserId(project.getId(), user.getId())) {
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

        invalidateMemberCache(project.getId());
    }

    private ProjectRole getRoleOrThrow(String roleName) {
        return projectRoleRepository.findByName(roleName)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR));
    }

    private boolean isOwner(ProjectMember member) {
        return "OWNER".equals(member.getProjectRole().getName());
    }

    private void invalidateMemberCache(UUID projectId) {
        redisService.delete(CacheKey.projectMembers(projectId));
    }

    private void publishAddMemberEvent(Project project, User user, User actor) {
        eventPublisher.publishEvent(
                NotificationEvent.builder()
                        .receiverId(user.getId())
                        .projectId(project.getId())
                        .title("Được thêm vào dự án")
                        .content(String.format("Bạn đã được %s thêm vào dự án '%s'",
                                actor.getFullName(), project.getName()))
                        .type("PROJECT_MEMBER_ADDED")
                        .referenceId(project.getId())
                        .actorId(actor.getId())
                        .build()
        );
    }
}