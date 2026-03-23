package com.projecttaskmanager.backend.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "project_invitations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectInvitation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String email;

    @Column(unique = true)
    private String token;

    @ManyToOne
    private Project project;

    @ManyToOne
    private ProjectRole role;

    private Instant expiredAt;
    private boolean accepted;
}