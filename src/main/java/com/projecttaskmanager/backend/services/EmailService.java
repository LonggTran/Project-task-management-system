package com.projecttaskmanager.backend.services;

public interface EmailService {
    void send(String to, String subject, String content);
}