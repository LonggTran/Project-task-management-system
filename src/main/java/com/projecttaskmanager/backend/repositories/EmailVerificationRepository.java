package com.projecttaskmanager.backend.repositories;

import com.projecttaskmanager.backend.models.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, String> {
    Optional<EmailVerification> findTopByEmailOrderByExpiryTimeDesc(String email);
}