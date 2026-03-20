package com.projecttaskmanager.backend.dto.request.label;

import lombok.Data;

@Data
public class CreateLabelRequest {
    private String name;
    private String color;
}