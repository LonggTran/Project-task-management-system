package com.projecttaskmanager.backend.repositories;

import com.projecttaskmanager.backend.models.Label;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LabelRepository extends JpaRepository<Label, UUID> {
}