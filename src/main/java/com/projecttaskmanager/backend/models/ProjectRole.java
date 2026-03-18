package com.projecttaskmanager.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "project_roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRole {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String name; // OWNER, ADMIN_PROJECT, MEMBER

    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "project_role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;
}
