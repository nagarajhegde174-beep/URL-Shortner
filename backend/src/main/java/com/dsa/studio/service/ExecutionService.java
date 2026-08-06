package com.dsa.studio.service;

import com.dsa.studio.dto.request.ExecuteRequest;
import com.dsa.studio.dto.response.CompileResponse;
import com.dsa.studio.dto.response.StepDebugInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExecutionService {

    private final CompilerService compilerService;
    private final JdiDebuggerService jdiDebuggerService;

    // Cache traces per session key (e.g. username or session identifier)
    private final Map<String, List<StepDebugInfo>> traceCache = new ConcurrentHashMap<>();
    private final Map<String, Integer> currentStepCache = new ConcurrentHashMap<>();

    public StepDebugInfo runAndInitialize(String sessionKey, ExecuteRequest request) {
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
            traceCache.put(sessionKey, Collections.singletonList(compileErrorStep));
            currentStepCache.put(sessionKey, 0);
            return compileErrorStep;
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

        traceCache.put(sessionKey, trace);
        currentStepCache.put(sessionKey, 0);

        return trace.get(0);
    }

    public StepDebugInfo getNextStep(String sessionKey) {
        List<StepDebugInfo> trace = traceCache.getOrDefault(sessionKey, Collections.emptyList());
        if (trace.isEmpty()) {
            return null;
        }

        int currentStep = currentStepCache.getOrDefault(sessionKey, 0);
        if (currentStep < trace.size() - 1) {
            currentStep++;
            currentStepCache.put(sessionKey, currentStep);
        }

        return trace.get(currentStep);
    }

    public StepDebugInfo getPreviousStep(String sessionKey) {
        List<StepDebugInfo> trace = traceCache.getOrDefault(sessionKey, Collections.emptyList());
        if (trace.isEmpty()) {
            return null;
        }

        int currentStep = currentStepCache.getOrDefault(sessionKey, 0);
        if (currentStep > 0) {
            currentStep--;
            currentStepCache.put(sessionKey, currentStep);
        }

        return trace.get(currentStep);
    }

    public StepDebugInfo getCurrentState(String sessionKey) {
        List<StepDebugInfo> trace = traceCache.getOrDefault(sessionKey, Collections.emptyList());
        if (trace.isEmpty()) {
            return null;
        }
        int currentStep = currentStepCache.getOrDefault(sessionKey, 0);
        return trace.get(currentStep);
    }

    public StepDebugInfo reset(String sessionKey) {
        if (traceCache.containsKey(sessionKey)) {
            currentStepCache.put(sessionKey, 0);
            return traceCache.get(sessionKey).get(0);
        }
        return null;
    }
}
