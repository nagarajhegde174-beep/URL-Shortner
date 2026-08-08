package com.dsa.studio.service;

import com.dsa.studio.dto.request.ExecuteRequest;
import com.dsa.studio.dto.response.CompileResponse;
import com.dsa.studio.dto.response.SessionStartResponse;
import com.dsa.studio.dto.response.StepDebugInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExecutionService {

    private final CompilerService compilerService;
    private final JdiDebuggerService jdiDebuggerService;

    // Cache traces and current pointers per session ID
    private final Map<String, List<StepDebugInfo>> traceCache = new ConcurrentHashMap<>();
    private final Map<String, Integer> currentStepCache = new ConcurrentHashMap<>();
    private final Map<String, String> sessionOwnerMap = new ConcurrentHashMap<>();

    public SessionStartResponse startSession(String username, ExecuteRequest request) {
        String sessionId = UUID.randomUUID().toString();
        sessionOwnerMap.put(sessionId, username);

        // Compile the code first
        CompileResponse compileResponse = compilerService.compile(request.getClassName(), request.getCode());
        if (!compileResponse.isSuccess()) {
            StepDebugInfo compileErrorStep = StepDebugInfo.builder()
                    .stepNumber(1)
                    .lineNumber(1)
                    .explanation("Compilation failed. Check errors.")
                    .exceptionName("CompilationException")
                    .exceptionMessage(compileResponse.getErrors())
                    .build();
            List<StepDebugInfo> singleErrorList = Collections.singletonList(compileErrorStep);
            traceCache.put(sessionId, singleErrorList);
            currentStepCache.put(sessionId, 0);

            return SessionStartResponse.builder()
                    .sessionId(sessionId)
                    .firstStep(compileErrorStep)
                    .build();
        }

        // Generate full trace
        List<StepDebugInfo> trace = jdiDebuggerService.generateTrace(request.getClassName(), request.getInput());
        if (trace.isEmpty()) {
            StepDebugInfo emptyStep = StepDebugInfo.builder()
                    .stepNumber(1)
                    .lineNumber(1)
                    .explanation("No execution steps recorded.")
                    .build();
            trace = Collections.singletonList(emptyStep);
        }

        traceCache.put(sessionId, trace);
        currentStepCache.put(sessionId, 0);

        return SessionStartResponse.builder()
                .sessionId(sessionId)
                .firstStep(trace.get(0))
                .build();
    }

    public boolean isOwner(String sessionId, String username) {
        String owner = sessionOwnerMap.get(sessionId);
        return owner != null && owner.equals(username);
    }

    public List<StepDebugInfo> getFullTrace(String sessionId) {
        return traceCache.getOrDefault(sessionId, Collections.emptyList());
    }

    public StepDebugInfo getNextStep(String sessionId) {
        List<StepDebugInfo> trace = traceCache.getOrDefault(sessionId, Collections.emptyList());
        if (trace.isEmpty()) {
            return null;
        }

        int currentStep = currentStepCache.getOrDefault(sessionId, 0);
        if (currentStep < trace.size() - 1) {
            currentStep++;
            currentStepCache.put(sessionId, currentStep);
        }

        return trace.get(currentStep);
    }

    public StepDebugInfo getPreviousStep(String sessionId) {
        List<StepDebugInfo> trace = traceCache.getOrDefault(sessionId, Collections.emptyList());
        if (trace.isEmpty()) {
            return null;
        }

        int currentStep = currentStepCache.getOrDefault(sessionId, 0);
        if (currentStep > 0) {
            currentStep--;
            currentStepCache.put(sessionId, currentStep);
        }

        return trace.get(currentStep);
    }

    public StepDebugInfo getCurrentState(String sessionId) {
        List<StepDebugInfo> trace = traceCache.getOrDefault(sessionId, Collections.emptyList());
        if (trace.isEmpty()) {
            return null;
        }
        int currentStep = currentStepCache.getOrDefault(sessionId, 0);
        return trace.get(currentStep);
    }

    public StepDebugInfo reset(String sessionId) {
        if (traceCache.containsKey(sessionId)) {
            currentStepCache.put(sessionId, 0);
            return traceCache.get(sessionId).get(0);
        }
        return null;
    }
}
