package com.dsa.studio.controller;

import com.dsa.studio.dto.request.PracticeSubmitRequest;
import com.dsa.studio.dto.response.ApiResponse;
import com.dsa.studio.dto.response.CompileResponse;
import com.dsa.studio.dto.response.PracticeProgressResponse;
import com.dsa.studio.dto.response.PracticeSubmitResponse;
import com.dsa.studio.dto.response.StepDebugInfo;
import com.dsa.studio.model.PracticeProblem;
import com.dsa.studio.model.User;
import com.dsa.studio.model.UserPracticeProgress;
import com.dsa.studio.repository.PracticeProblemRepository;
import com.dsa.studio.repository.UserPracticeProgressRepository;
import com.dsa.studio.repository.UserRepository;
import com.dsa.studio.security.UserDetailsImpl;
import com.dsa.studio.service.CompilerService;
import com.dsa.studio.service.JdiDebuggerService;
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
@RequestMapping("/api/practice")
@RequiredArgsConstructor
@Tag(name = "Practice", description = "Coding practice problems and user progress tracking APIs")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class PracticeController {

    private final PracticeProblemRepository practiceProblemRepository;
    private final UserPracticeProgressRepository userPracticeProgressRepository;
    private final UserRepository userRepository;
    private final CompilerService compilerService;
    private final JdiDebuggerService jdiDebuggerService;

    @GetMapping("/problems")
    @Operation(summary = "Get list of all practice problems")
    public ResponseEntity<ApiResponse<List<PracticeProblem>>> getProblems() {
        List<PracticeProblem> problems = practiceProblemRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success("Problems fetched successfully", problems));
    }

    @GetMapping("/progress")
    @Operation(summary = "Get current user practice problem progress")
    public ResponseEntity<ApiResponse<List<PracticeProgressResponse>>> getProgress(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        List<PracticeProblem> problems = practiceProblemRepository.findAll();
        List<UserPracticeProgress> progressList = userPracticeProgressRepository.findByUserId(Objects.requireNonNull(currentUser.getId(), "User ID cannot be null"));
        List<PracticeProgressResponse> responses = new ArrayList<>();

        for (PracticeProblem p : problems) {
            Optional<UserPracticeProgress> progressOpt = progressList.stream()
                    .filter(pr -> pr.getPracticeProblem().getId().equals(p.getId()))
                    .findFirst();

            PracticeProgressResponse.PracticeProgressResponseBuilder builder = PracticeProgressResponse.builder()
                    .problemId(p.getId())
                    .title(p.getTitle())
                    .difficulty(p.getDifficulty());

            if (progressOpt.isPresent()) {
                UserPracticeProgress pr = progressOpt.get();
                builder.attempts(pr.getAttempts())
                        .successfulAttempts(pr.getSuccessfulAttempts())
                        .solved(pr.isSolved())
                        .accuracy(pr.getAccuracy());
            } else {
                builder.attempts(0)
                        .successfulAttempts(0)
                        .solved(false)
                        .accuracy(0.0);
            }

            responses.add(builder.build());
        }

        return ResponseEntity.ok(ApiResponse.success("Progress fetched successfully", responses));
    }

    @PostMapping("/problems/{problemId}/submit")
    @Operation(summary = "Submit a java solution for execution and verification")
    public ResponseEntity<ApiResponse<PracticeSubmitResponse>> submitSolution(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable Long problemId,
            @RequestBody PracticeSubmitRequest request) {

        PracticeProblem problem = practiceProblemRepository.findById(Objects.requireNonNull(problemId, "Problem ID cannot be null"))
                .orElseThrow(() -> new RuntimeException("Problem not found"));
        User user = userRepository.findById(Objects.requireNonNull(currentUser.getId(), "User ID cannot be null"))
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Compile user code
        CompileResponse compileResponse = compilerService.compile(problem.getClassName(), request.getCode());

        UserPracticeProgress progress = userPracticeProgressRepository.findByUserIdAndPracticeProblemId(user.getId(), problem.getId())
                .orElseGet(() -> {
                    UserPracticeProgress newP = new UserPracticeProgress();
                    newP.setUser(user);
                    newP.setPracticeProblem(problem);
                    newP.setAttempts(0);
                    newP.setSuccessfulAttempts(0);
                    newP.setSolved(false);
                    newP.setAccuracy(0.0);
                    return newP;
                });

        progress.setAttempts(progress.getAttempts() + 1);

        PracticeProgressResponse.PracticeProgressResponseBuilder progressBuilder = PracticeProgressResponse.builder()
                .problemId(problem.getId())
                .title(problem.getTitle())
                .difficulty(problem.getDifficulty());

        if (!compileResponse.isSuccess()) {
            progress.setAccuracy((double) progress.getSuccessfulAttempts() / progress.getAttempts() * 100.0);
            userPracticeProgressRepository.save(progress);

            return ResponseEntity.ok(ApiResponse.success("Compilation failed", PracticeSubmitResponse.builder()
                    .success(false)
                    .solved(progress.isSolved())
                    .feedback("Compilation Error")
                    .compileError(compileResponse.getErrors())
                    .progress(progressBuilder
                            .attempts(progress.getAttempts())
                            .successfulAttempts(progress.getSuccessfulAttempts())
                            .solved(progress.isSolved())
                            .accuracy(progress.getAccuracy())
                            .build())
                    .build()));
        }

        // Run user code
        List<StepDebugInfo> trace = jdiDebuggerService.generateTrace(problem.getClassName(), "");
        String programOutput = "";
        String runtimeError = null;

        if (!trace.isEmpty()) {
            StepDebugInfo lastStep = trace.get(trace.size() - 1);
            programOutput = lastStep.getOutput() != null ? lastStep.getOutput() : "";
            if (lastStep.getExceptionMessage() != null) {
                runtimeError = lastStep.getExceptionName() + ": " + lastStep.getExceptionMessage();
            }
        }

        boolean solved = false;
        String feedback = "";
        if (runtimeError != null) {
            feedback = "Runtime Error: " + runtimeError;
        } else {
            String expected = problem.getExpectedOutput() != null ? problem.getExpectedOutput().trim() : "";
            String actual = programOutput.trim();
            if (expected.equals(actual)) {
                solved = true;
                feedback = "Accepted!";
            } else {
                feedback = "Wrong Answer. Expected: [" + expected + "], Got: [" + actual + "]";
            }
        }

        if (solved) {
            progress.setSuccessfulAttempts(progress.getSuccessfulAttempts() + 1);
            progress.setSolved(true);
        }
        progress.setAccuracy((double) progress.getSuccessfulAttempts() / progress.getAttempts() * 100.0);
        userPracticeProgressRepository.save(progress);

        return ResponseEntity.ok(ApiResponse.success("Submission processed", PracticeSubmitResponse.builder()
                .success(runtimeError == null && solved)
                .solved(progress.isSolved())
                .feedback(feedback)
                .output(programOutput)
                .progress(progressBuilder
                        .attempts(progress.getAttempts())
                        .successfulAttempts(progress.getSuccessfulAttempts())
                        .solved(progress.isSolved())
                        .accuracy(progress.getAccuracy())
                        .build())
                .build()));
    }
}
