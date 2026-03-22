package com.projecttaskmanager.backend.dto.response.task;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class SubtaskTreeResponse {
    private SubtaskResponse task;
    private List<SubtaskTreeResponse> children;
}