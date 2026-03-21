package com.projecttaskmanager.backend.models;

import com.projecttaskmanager.backend.models.baseModels.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "comments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private Task task;

    @ManyToOne
    private User user;

    @Column(columnDefinition = "TEXT")
    private String content;
}