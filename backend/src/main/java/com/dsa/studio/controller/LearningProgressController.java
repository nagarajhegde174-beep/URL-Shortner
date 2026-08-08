package com.dsa.studio.controller;

import com.dsa.studio.dto.request.LearningProgressUpdateRequest;
import com.dsa.studio.dto.response.ApiResponse;
import com.dsa.studio.dto.response.LearningProgressResponse;
import com.dsa.studio.model.Algorithm;
import com.dsa.studio.model.LearningProgress;
import com.dsa.studio.model.User;
import com.dsa.studio.repository.AlgorithmRepository;
import com.dsa.studio.repository.LearningProgressRepository;
import com.dsa.studio.repository.UserRepository;
import com.dsa.studio.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/learning-progress")
@RequiredArgsConstructor
@Tag(name = "Learning Progress", description = "DSA algorithms learning progress tracking APIs")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class LearningProgressController {

    private final LearningProgressRepository learningProgressRepository;
    private final AlgorithmRepository algorithmRepository;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get current user learning progress for all algorithms")
    public ResponseEntity<ApiResponse<List<LearningProgressResponse>>> getLearningProgress(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        List<Algorithm> algorithms = algorithmRepository.findAll();
        List<LearningProgress> progressList = learningProgressRepository.findByUserId(currentUser.getId());
        List<LearningProgressResponse> responses = new ArrayList<>();

        for (Algorithm a : algorithms) {
            Optional<LearningProgress> progressOpt = progressList.stream()
                    .filter(p -> p.getAlgorithm().getId().equals(a.getId()))
                    .findFirst();

            LearningProgressResponse.LearningProgressResponseBuilder builder = LearningProgressResponse.builder()
                    .algorithmId(a.getId())
                    .name(a.getName())
                    .category(a.getCategory());

            if (progressOpt.isPresent()) {
                LearningProgress p = progressOpt.get();
                builder.completionPercentage(p.getCompletionPercentage())
                        .completed(p.isCompleted());
            } else {
                builder.completionPercentage(0)
                        .completed(false);
            }

            responses.add(builder.build());
        }

        return ResponseEntity.ok(ApiResponse.success("Learning progress fetched successfully", responses));
    }

    @PostMapping("/update")
    @Operation(summary = "Update learning progress completion percentage for an algorithm")
    public ResponseEntity<ApiResponse<LearningProgress>> updateLearningProgress(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestBody LearningProgressUpdateRequest request) {

        User user = userRepository.findById(Objects.requireNonNull(currentUser.getId(), "User ID cannot be null"))
                .orElseThrow(() -> new RuntimeException("User not found"));
        Algorithm algorithm = algorithmRepository.findById(Objects.requireNonNull(request.getAlgorithmId(), "Algorithm ID cannot be null"))
                .orElseThrow(() -> new RuntimeException("Algorithm not found"));

        LearningProgress progress = learningProgressRepository
                .findByUserIdAndAlgorithmId(user.getId(), algorithm.getId())
                .orElseGet(() -> {
                    LearningProgress newP = new LearningProgress();
                    newP.setUser(user);
                    newP.setAlgorithm(algorithm);
                    newP.setCompletionPercentage(0);
                    newP.setCompleted(false);
                    newP.setTimeSpentMinutes(0L);
                    return newP;
                });

        progress.setCompletionPercentage(request.getCompletionPercentage());
        progress.setCompleted(request.isCompleted());
        if (request.getCompletionPercentage() >= 100) {
            progress.setCompleted(true);
        }

        LearningProgress savedProgress = learningProgressRepository.save(progress);
        return ResponseEntity.ok(ApiResponse.success("Learning progress updated successfully", savedProgress));
    }
}
