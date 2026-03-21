package com.projecttaskmanager.backend.dto.request.epic;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateEpicRequest {
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
}