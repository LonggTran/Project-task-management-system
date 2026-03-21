package com.projecttaskmanager.backend.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "workflow_steps")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowStep {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private Workflow workflow;

    @ManyToOne
    @JoinColumn(name = "from_status_id")
    private TaskStatus fromStatus;

    @ManyToOne
    @JoinColumn(name = "to_status_id")
    private TaskStatus toStatus;

    private String requiredPermission;
}