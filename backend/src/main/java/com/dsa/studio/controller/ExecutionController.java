package com.dsa.studio.controller;

import com.dsa.studio.dto.request.ExecuteRequest;
import com.dsa.studio.dto.response.ApiResponse;
import com.dsa.studio.dto.response.StepDebugInfo;
import com.dsa.studio.security.UserDetailsImpl;
import com.dsa.studio.service.ExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/execution")
@RequiredArgsConstructor
@Tag(name = "Execution", description = "Debugging and step execution control endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class ExecutionController {

    private final ExecutionService executionService;

    @PostMapping("/run")
    @Operation(summary = "Start a fresh execution session and load debug trace")
    public ResponseEntity<ApiResponse<StepDebugInfo>> run(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Valid @RequestBody ExecuteRequest request) {
        String sessionKey = currentUser.getUsername();
        StepDebugInfo firstStep = executionService.runAndInitialize(sessionKey, request);
        return ResponseEntity.ok(ApiResponse.success("Execution trace initialized", firstStep));
    }

    @PostMapping("/step")
    @Operation(summary = "Step forward or backward through execution steps")
    public ResponseEntity<ApiResponse<StepDebugInfo>> step(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestParam(defaultValue = "next") String direction) {
        String sessionKey = currentUser.getUsername();
        StepDebugInfo stepInfo;
        if ("prev".equalsIgnoreCase(direction)) {
            stepInfo = executionService.getPreviousStep(sessionKey);
        } else {
            stepInfo = executionService.getNextStep(sessionKey);
        }

        if (stepInfo == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("No active execution session. Call /run first."));
        }
        return ResponseEntity.ok(ApiResponse.success("Stepped successfully", stepInfo));
    }

    @PostMapping("/reset")
    @Operation(summary = "Reset current execution trace pointer back to the first step")
    public ResponseEntity<ApiResponse<StepDebugInfo>> reset(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String sessionKey = currentUser.getUsername();
        StepDebugInfo stepInfo = executionService.reset(sessionKey);
        if (stepInfo == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("No active execution session. Call /run first."));
        }
        return ResponseEntity.ok(ApiResponse.success("Trace reset successful", stepInfo));
    }

    @GetMapping("/state")
    @Operation(summary = "Retrieve current execution step state")
    public ResponseEntity<ApiResponse<StepDebugInfo>> state(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String sessionKey = currentUser.getUsername();
        StepDebugInfo stepInfo = executionService.getCurrentState(sessionKey);
        if (stepInfo == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("No active execution session. Call /run first."));
        }
        return ResponseEntity.ok(ApiResponse.success("Current state retrieved", stepInfo));
    }
}
