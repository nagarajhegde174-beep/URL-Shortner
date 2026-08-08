package com.dsa.studio.controller;

import com.dsa.studio.dto.request.ExecuteRequest;
import com.dsa.studio.dto.response.ApiResponse;
import com.dsa.studio.dto.response.SessionStartResponse;
import com.dsa.studio.dto.response.StepDebugInfo;
import com.dsa.studio.security.UserDetailsImpl;
import com.dsa.studio.service.ExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/execution")
@RequiredArgsConstructor
@Tag(name = "Execution", description = "Session-based debugging and step execution control endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class ExecutionController {

    private final ExecutionService executionService;

    @PostMapping("/start")
    @Operation(summary = "Start a fresh session-based execution and load debug trace")
    public ResponseEntity<ApiResponse<SessionStartResponse>> startSession(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Valid @RequestBody ExecuteRequest request) {
        String username = currentUser.getUsername();
        SessionStartResponse response = executionService.startSession(username, request);
        return ResponseEntity.ok(ApiResponse.success("Execution session started", response));
    }

    @GetMapping("/{sessionId}/trace")
    @Operation(summary = "Retrieve the full execution trace for the session")
    public ResponseEntity<ApiResponse<List<StepDebugInfo>>> getTrace(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable String sessionId) {
        String username = currentUser.getUsername();
        if (!executionService.isOwner(sessionId, username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied. This execution session does not belong to you."));
        }
        List<StepDebugInfo> trace = executionService.getFullTrace(sessionId);
        return ResponseEntity.ok(ApiResponse.success("Full trace retrieved", trace));
    }

    @GetMapping("/{sessionId}/state")
    @Operation(summary = "Retrieve current execution step state")
    public ResponseEntity<ApiResponse<StepDebugInfo>> getState(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable String sessionId) {
        String username = currentUser.getUsername();
        if (!executionService.isOwner(sessionId, username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied. This execution session does not belong to you."));
        }
        StepDebugInfo stepInfo = executionService.getCurrentState(sessionId);
        if (stepInfo == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("No active session trace found."));
        }
        return ResponseEntity.ok(ApiResponse.success("Current state retrieved", stepInfo));
    }

    @PostMapping("/{sessionId}/step")
    @Operation(summary = "Step forward or backward through execution steps")
    public ResponseEntity<ApiResponse<StepDebugInfo>> step(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "next") String direction) {
        String username = currentUser.getUsername();
        if (!executionService.isOwner(sessionId, username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied. This execution session does not belong to you."));
        }
        StepDebugInfo stepInfo;
        if ("prev".equalsIgnoreCase(direction)) {
            stepInfo = executionService.getPreviousStep(sessionId);
        } else {
            stepInfo = executionService.getNextStep(sessionId);
        }

        if (stepInfo == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("No steps available."));
        }
        return ResponseEntity.ok(ApiResponse.success("Stepped successfully", stepInfo));
    }

    @PostMapping("/{sessionId}/reset")
    @Operation(summary = "Reset session trace pointer back to the first step")
    public ResponseEntity<ApiResponse<StepDebugInfo>> reset(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable String sessionId) {
        String username = currentUser.getUsername();
        if (!executionService.isOwner(sessionId, username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied. This execution session does not belong to you."));
        }
        StepDebugInfo stepInfo = executionService.reset(sessionId);
        if (stepInfo == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("No active session trace found."));
        }
        return ResponseEntity.ok(ApiResponse.success("Trace reset successful", stepInfo));
    }
}
