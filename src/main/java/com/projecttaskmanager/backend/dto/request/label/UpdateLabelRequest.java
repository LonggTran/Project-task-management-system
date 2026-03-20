package com.projecttaskmanager.backend.dto.request.label;

import lombok.Data;

@Data
public class UpdateLabelRequest {
    private String name;
    private String color;
}