package com.projecttaskmanager.backend.models;

import com.projecttaskmanager.backend.models.baseModels.ProjectMemberId;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "project_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMember {
    @EmbeddedId
    private ProjectMemberId id;

    @ManyToOne
    @MapsId("projectId")
    private Project project;

    @ManyToOne
    @MapsId("userId")
    private User user;

    @ManyToOne
    @JoinColumn(name = "project_role_id")
    private ProjectRole projectRole;

    private Instant joinedAt;
}
