package com.dsa.studio.controller;

import com.dsa.studio.dto.request.QuizSubmitRequest;
import com.dsa.studio.dto.response.ApiResponse;
import com.dsa.studio.model.User;
import com.dsa.studio.model.UserQuizAttempt;
import com.dsa.studio.repository.UserQuizAttemptRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
@Tag(name = "Quiz", description = "Quiz attempts and progress tracking APIs")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class QuizController {

    private final UserQuizAttemptRepository userQuizAttemptRepository;
    private final UserRepository userRepository;

    @GetMapping("/progress")
    @Operation(summary = "Get list of all quiz attempts by current user")
    public ResponseEntity<ApiResponse<List<UserQuizAttempt>>> getQuizProgress(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        List<UserQuizAttempt> attempts = userQuizAttemptRepository.findByUserId(Objects.requireNonNull(currentUser.getId(), "User ID cannot be null"));
        return ResponseEntity.ok(ApiResponse.success("Quiz progress fetched successfully", attempts));
    }

    @PostMapping("/submit")
    @Operation(summary = "Submit score for a completed quiz")
    public ResponseEntity<ApiResponse<UserQuizAttempt>> submitQuizResult(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestBody QuizSubmitRequest request) {

        User user = userRepository.findById(Objects.requireNonNull(currentUser.getId(), "User ID cannot be null"))
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserQuizAttempt attempt = new UserQuizAttempt();
        attempt.setUser(user);
        attempt.setQuizTitle(request.getQuizTitle());
        attempt.setCategory(request.getCategory() != null ? request.getCategory() : "ARRAY");
        attempt.setScore(request.getScore());
        attempt.setTotalQuestions(request.getTotalQuestions());
        attempt.setPercentage(request.getTotalQuestions() > 0 ? 
                ((double) request.getScore() / request.getTotalQuestions() * 100.0) : 0.0);
        attempt.setCompleted(true);
        attempt.setAttemptDate(LocalDateTime.now());

        UserQuizAttempt savedAttempt = userQuizAttemptRepository.save(attempt);
        return ResponseEntity.ok(ApiResponse.success("Quiz score submitted successfully", savedAttempt));
    }
}
