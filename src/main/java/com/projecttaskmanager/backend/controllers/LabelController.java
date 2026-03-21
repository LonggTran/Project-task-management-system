package com.projecttaskmanager.backend.controllers;

import com.projecttaskmanager.backend.dto.request.label.CreateLabelRequest;
import com.projecttaskmanager.backend.dto.request.label.UpdateLabelRequest;
import com.projecttaskmanager.backend.dto.response.ApiResponse;
import com.projecttaskmanager.backend.dto.response.label.LabelResponse;
import com.projecttaskmanager.backend.services.LabelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/labels")
@RequiredArgsConstructor
public class LabelController {

    private final LabelService labelService;

    @PostMapping("/{projectId}")
    public ResponseEntity<ApiResponse<LabelResponse>> createLabel(@PathVariable UUID projectId,
                                                                  @RequestBody CreateLabelRequest request) {
        LabelResponse response = labelService.createLabel(request, projectId);
        return ResponseEntity.ok(ApiResponse.<LabelResponse>builder()
                .success(true)
                .message("Label created")
                .data(response)
                .timestamp(Instant.now())
                .build());
    }

    @PutMapping("/{projectId}/{labelId}")
    public ResponseEntity<ApiResponse<LabelResponse>> updateLabel(@PathVariable UUID projectId,
                                                                  @PathVariable UUID labelId,
                                                                  @RequestBody UpdateLabelRequest request) {
        LabelResponse response = labelService.updateLabel(labelId, request, projectId);
        return ResponseEntity.ok(ApiResponse.<LabelResponse>builder()
                .success(true)
                .message("Label updated")
                .data(response)
                .timestamp(Instant.now())
                .build());
    }

    @DeleteMapping("/{projectId}/{labelId}")
    public ResponseEntity<ApiResponse<Void>> deleteLabel(@PathVariable UUID projectId,
                                                         @PathVariable UUID labelId) {
        labelService.deleteLabel(labelId, projectId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Label deleted")
                .timestamp(Instant.now())
                .build());
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ApiResponse<List<LabelResponse>>> getAllLabels(@PathVariable UUID projectId) {
        List<LabelResponse> labels = labelService.getAllLabels(projectId);
        return ResponseEntity.ok(ApiResponse.<List<LabelResponse>>builder()
                .success(true)
                .message("Labels fetched")
                .data(labels)
                .timestamp(Instant.now())
                .build());
    }

    @GetMapping("/{projectId}/{labelId}")
    public ResponseEntity<ApiResponse<LabelResponse>> getLabelById(@PathVariable UUID projectId,
                                                                   @PathVariable UUID labelId) {
        LabelResponse response = labelService.getLabelById(labelId, projectId);
        return ResponseEntity.ok(ApiResponse.<LabelResponse>builder()
                .success(true)
                .message("Label fetched")
                .data(response)
                .timestamp(Instant.now())
                .build());
    }
}