package com.projecttaskmanager.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ApiResponse<T> {
    private Boolean success;
    private String message;
    private String errorCode;
    private T data;
    private Instant timestamp;
}