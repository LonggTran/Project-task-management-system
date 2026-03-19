package com.projecttaskmanager.backend.exceptions;

import lombok.Getter;

@Getter
public enum ErrorCode {

    USER_NOT_FOUND("USER_NOT_FOUND", "User not found"),

    PROJECT_NOT_FOUND("PROJECT_NOT_FOUND", "Project not found"),

    TASK_NOT_FOUND("TASK_NOT_FOUND", "Task not found"),

    UNAUTHORIZED("UNAUTHORIZED", "Unauthorized access"),

    FORBIDDEN("FORBIDDEN", "Access denied"),

    INTERNAL_ERROR("INTERNAL_ERROR", "Internal server error"),

    VALIDATION_ERROR("VALIDATION_ERROR", "Validation failed"),

    EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS", "Email already exists"),

    USER_ALREADY_IN_PROJECT("USER_ALREADY_IN_PROJECT", "User already in project");

    private final String code;

    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}