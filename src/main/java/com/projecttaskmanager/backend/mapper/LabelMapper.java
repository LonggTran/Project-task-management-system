package com.projecttaskmanager.backend.mapper;

import com.projecttaskmanager.backend.dto.response.label.LabelResponse;
import com.projecttaskmanager.backend.models.Label;
import org.springframework.stereotype.Component;

@Component
public class LabelMapper {

    public LabelResponse toResponse(Label label) {
        if (label == null) return null;

        return LabelResponse.builder()
                .id(label.getId())
                .name(label.getName())
                .color(label.getColor())
                .build();
    }
}