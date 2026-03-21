package com.projecttaskmanager.backend.dto.response.task;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SubtaskTreeResponse {
    private SubtaskResponse task;
    private List<SubtaskTreeResponse> children;
}