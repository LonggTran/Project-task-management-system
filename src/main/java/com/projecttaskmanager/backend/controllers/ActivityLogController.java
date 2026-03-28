package com.projecttaskmanager.backend.controllers;

import com.projecttaskmanager.backend.dto.response.ApiResponse;
import com.projecttaskmanager.backend.dto.response.activity.ActivityResponse;
import com.projecttaskmanager.backend.services.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/activities")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService activityService;

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<List<ActivityResponse>>> getProjectActivities(
            @PathVariable UUID projectId) {
        return ResponseEntity.ok(ApiResponse.<List<ActivityResponse>>builder()
                .success(true)
                .data(activityService.getProjectActivities(projectId))
                .timestamp(Instant.now())
                .build());
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<ActivityResponse>>> getMyActivities() {
        return ResponseEntity.ok(ApiResponse.<List<ActivityResponse>>builder()
                .success(true)
                .data(activityService.getMyActivities())
                .timestamp(Instant.now())
                .build());
    }
}