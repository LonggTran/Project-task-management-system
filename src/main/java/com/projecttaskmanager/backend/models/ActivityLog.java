package com.projecttaskmanager.backend.models;

import com.projecttaskmanager.backend.models.enums.ActivityAction;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "activity_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private ActivityAction action;

    private String entityType;

    private UUID entityId;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne
    @JoinColumn(name = "actor_id")
    private User actor;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    private Instant createdAt;
}