package com.dsa.studio.service;

import com.dsa.studio.dto.response.StackFrameInfo;
import com.dsa.studio.dto.response.StepDebugInfo;
import com.dsa.studio.dto.response.StepMetadata;
import com.dsa.studio.dto.response.VariableInfo;
import com.sun.jdi.*;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class JdiDebuggerService {

    private final CompilerService compilerService;

    // Simulated memory address map to keep consistent object addresses for visualization
    private final Map<String, String> objectMemoryAddressMap = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public List<StepDebugInfo> generateTrace(String className, String inputData) {
        List<StepDebugInfo> trace = new ArrayList<>();
        objectMemoryAddressMap.clear();

        // 1. Get workspace directory
        String classpath = compilerService.getWorkspaceDir();

        List<String> sourceLines = new ArrayList<>();
        try {
            sourceLines = java.nio.file.Files.readAllLines(java.nio.file.Paths.get(classpath, className + ".java"), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("Could not read source code for trace metadata: {}", e.getMessage());
        }

        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        LaunchingConnector connector = vmm.defaultConnector();
        Map<String, Connector.Argument> arguments = connector.defaultArguments();

        Connector.Argument mainArg = arguments.get("main");
        Connector.Argument optionsArg = arguments.get("options");

        if (mainArg != null) {
            mainArg.setValue(className);
        }
        if (optionsArg != null) {
            optionsArg.setValue("-cp \"" + classpath + "\"");
        }

        try {
            VirtualMachine vm = connector.launch(arguments);
            Process process = vm.process();

            // Setup asynchronous readers for stdout and stderr
            StringBuilder stdoutBuffer = new StringBuilder();
            StringBuilder stderrBuffer = new StringBuilder();
            Thread stdoutThread = startStreamReader(process.getInputStream(), stdoutBuffer);
            Thread stderrThread = startStreamReader(process.getErrorStream(), stderrBuffer);

            EventRequestManager erm = vm.eventRequestManager();

            // Set prepare request for user class
            ClassPrepareRequest classPrepareRequest = erm.createClassPrepareRequest();
            classPrepareRequest.addClassFilter(className);
            classPrepareRequest.enable();

            EventQueue eventQueue = vm.eventQueue();
            boolean running = true;
            int stepCounter = 0;
            final int MAX_STEPS = 500; // safety ceiling

            Map<String, String> previousVariableValues = new HashMap<>();

            vm.resume();

            while (running && stepCounter < MAX_STEPS) {
                EventSet eventSet = eventQueue.remove(1000); // 1-second timeout
                if (eventSet == null) {
                    // Check if process finished
                    if (!process.isAlive()) {
                        break;
                    }
                    continue;
                }

                for (Event event : eventSet) {
                    if (event instanceof VMDisconnectEvent || event instanceof VMDeathEvent) {
                        running = false;
                    } 
                    else if (event instanceof ClassPrepareEvent cpe) {
                        // Create a step request for the specific thread that triggered
                        // class preparation. Using vm.allThreads() with a status check
                        // is unreliable — the main thread is THREAD_STATUS_RUNNING even
                        // when suspended by the debugger, so it was being silently skipped.
                        createStepRequest(erm, cpe.thread());
                    } 
                    else if (event instanceof StepEvent stepEvent) {
                        Location location = stepEvent.location();
                        String locClassName = location.declaringType().name();

                        // Only record steps within our compiled user class and ignore standard library classes
                        if (locClassName.equals(className)) {
                            stepCounter++;
                            ThreadReference thread = stepEvent.thread();
                            
                            String codeLine = "";
                            int lineIndex = location.lineNumber() - 1;
                            if (lineIndex >= 0 && lineIndex < sourceLines.size()) {
                                codeLine = sourceLines.get(lineIndex);
                            }

                            // Capture current state
                            StepDebugInfo debugInfo = captureState(
                                    stepCounter,
                                    location.lineNumber(),
                                    thread,
                                    stdoutBuffer.toString(),
                                    previousVariableValues,
                                    codeLine,
                                    className
                            );
                            
                            trace.add(debugInfo);

                            // Re-create step requests if necessary
                            erm.deleteEventRequest(event.request());
                            createStepRequest(erm, thread);
                        }
                    }
                }
                vm.resume();
            }

            // Cleanup
            try {
                vm.exit(0);
            } catch (Exception ignored) {}

            process.destroy();
            stdoutThread.join(500);
            stderrThread.join(500);

            // Add final step showing exit state if trace isn't empty
            if (!trace.isEmpty()) {
                StepDebugInfo last = trace.get(trace.size() - 1);
                String err = stderrBuffer.toString();
                if (!err.isEmpty() && last.getExceptionMessage() == null) {
                    last.setExceptionName("Runtime Error / Standard Error Output");
                    last.setExceptionMessage(err);
                }
            }

        } catch (Exception e) {
            log.error("JDI Debugging execution failed: ", e);
            // Return failure step
            trace.add(StepDebugInfo.builder()
                    .stepNumber(1)
                    .lineNumber(1)
                    .output("")
                    .explanation("Failed to execute code: " + e.getMessage())
                    .exceptionName(e.getClass().getSimpleName())
                    .exceptionMessage(e.getMessage())
                    .build());
        }

        return trace;
    }

    private void createStepRequest(EventRequestManager erm, ThreadReference thread) {
        // Step into line-by-line
        StepRequest stepRequest = erm.createStepRequest(thread, StepRequest.STEP_LINE, StepRequest.STEP_INTO);
        stepRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        stepRequest.enable();
    }

    private Thread startStreamReader(InputStream is, StringBuilder buffer) {
        Thread thread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }
            } catch (Exception e) {
                log.error("Error reading process stream", e);
            }
        });
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    private StepDebugInfo captureState(int stepNum, int lineNum, ThreadReference thread, String currentOutput, Map<String, String> prevVals, String codeLine, String className) {
        List<StackFrameInfo> callStack = new ArrayList<>();
        List<VariableInfo> variables = new ArrayList<>();
        String explanation = "Executing line " + lineNum;

        try {
            List<StackFrame> frames = thread.frames();
            for (StackFrame frame : frames) {
                // Stack Frame details
                Location loc = frame.location();
                String methodName = loc.method().name();
                
                List<VariableInfo> frameVariables = new ArrayList<>();
                try {
                    List<LocalVariable> visibleVars = frame.visibleVariables();
                    for (LocalVariable var : visibleVars) {
                        Value val = frame.getValue(var);
                        String name = var.name();
                        String type = var.typeName();
                        String valStr = formatValue(val);

                        // Memory Address (Simulated)
                        String memAddr = "0x" + Integer.toHexString(System.identityHashCode(var));
                        if (val instanceof ObjectReference objRef) {
                            memAddr = getOrCreateAddress(objRef);
                        }

                        // Status updates
                        String status = "UNCHANGED";
                        String prevVal = prevVals.get(name);
                        if (prevVal == null) {
                            status = "NEW";
                        } else if (!prevVal.equals(valStr)) {
                            status = "UPDATED";
                        }
                        prevVals.put(name, valStr);

                        VariableInfo varInfo = VariableInfo.builder()
                                .name(name)
                                .type(type)
                                .value(valStr)
                                .previousValue(prevVal)
                                .memoryAddress(memAddr)
                                .scope(methodName)
                                .status(status)
                                .build();

                        frameVariables.add(varInfo);
                        // Add to flat list for variables panel convenience
                        variables.add(varInfo);
                    }
                } catch (AbsentInformationException e) {
                    // Debug info missing for current method/frame
                }

                callStack.add(StackFrameInfo.builder()
                        .methodName(methodName)
                        .lineNumber(loc.lineNumber())
                        .localVariables(frameVariables)
                        .build());
            }

            // Generate simple natural English explanation for educational visualization
            if (!callStack.isEmpty()) {
                StackFrameInfo top = callStack.get(0);
                explanation = String.format("Program is at line %d in method '%s'. Visible variables: %s",
                        lineNum, top.getMethodName(), 
                        variables.isEmpty() ? "none" : variables.stream().map(v -> v.getName() + "=" + v.getValue()).toList());
            }

        } catch (IncompatibleThreadStateException e) {
            log.error("Failed to capture stack state: {}", e.getMessage());
        }

        StepMetadata metadata = extractMetadata(className, codeLine, variables);

        return StepDebugInfo.builder()
                .stepNumber(stepNum)
                .lineNumber(lineNum)
                .callStack(callStack)
                .variables(variables)
                .output(currentOutput)
                .explanation(explanation)
                .metadata(metadata)
                .build();
    }

    private StepMetadata extractMetadata(String className, String codeLine, List<VariableInfo> variables) {
        String dataStructure = "ARRAY";
        
        Map<String, Integer> pointers = new HashMap<>();
        List<String> pointerNames = List.of("i", "j", "left", "right", "low", "high", "mid", "start", "end", "p1", "p2", "windowStart", "windowEnd", "minIdx", "maxIdx");
        for (VariableInfo v : variables) {
            if (pointerNames.contains(v.getName())) {
                try {
                    pointers.put(v.getName(), Integer.parseInt(v.getValue()));
                } catch (NumberFormatException ignored) {}
            }
        }
        
        List<Integer> indices = new ArrayList<>();
        if (codeLine != null) {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\[([a-zA-Z0-9_]+)\\]");
            java.util.regex.Matcher matcher = pattern.matcher(codeLine);
            while (matcher.find()) {
                String idxVarName = matcher.group(1);
                if (idxVarName.matches("\\d+")) {
                    indices.add(Integer.parseInt(idxVarName));
                } else {
                    for (VariableInfo v : variables) {
                        if (v.getName().equals(idxVarName)) {
                            try {
                                indices.add(Integer.parseInt(v.getValue()));
                            } catch (NumberFormatException ignored) {}
                            break;
                        }
                    }
                }
            }
        }
        
        String operation = "READ";
        if (codeLine != null) {
            String trimmedLine = codeLine.trim();
            if (trimmedLine.contains("swap") || className.toLowerCase().contains("reverse") || className.toLowerCase().contains("rotate")) {
                if (trimmedLine.contains("arr[") && trimmedLine.contains("=")) {
                    operation = "SWAP";
                }
            }
            if (operation.equals("READ")) {
                if (trimmedLine.contains("==") || trimmedLine.contains("<") || trimmedLine.contains(">") || trimmedLine.contains("!=") || trimmedLine.contains("<=") || trimmedLine.contains(">=")) {
                    operation = "COMPARE";
                } else if (trimmedLine.matches(".*\\b(i|j|left|right|low|high|mid|start|end)\\s*(\\+\\+|\\-\\-|\\+=|\\-=|=).*")) {
                    operation = "POINTER_MOVE";
                } else if (trimmedLine.contains("insert") || trimmedLine.contains("size++")) {
                    operation = "INSERT";
                } else if (trimmedLine.contains("delete") || trimmedLine.contains("remove") || trimmedLine.contains("size--")) {
                    operation = "DELETE";
                } else if (trimmedLine.contains("windowSum") || trimmedLine.contains("windowEnd") || trimmedLine.contains("windowStart")) {
                    operation = "WINDOW_UPDATE";
                } else if (trimmedLine.contains("arr[") && trimmedLine.contains("=") && !trimmedLine.contains("==")) {
                    int equalsIndex = trimmedLine.indexOf('=');
                    int arrayIndex = trimmedLine.indexOf("arr[");
                    if (arrayIndex != -1 && arrayIndex < equalsIndex) {
                        operation = "WRITE";
                    }
                }
            }
        }
        
        return StepMetadata.builder()
                .dataStructure(dataStructure)
                .operation(operation)
                .indices(indices)
                .pointers(pointers)
                .build();
    }

    private String formatValue(Value value) {
        if (value == null) return "null";
        if (value instanceof StringReference strRef) {
            return "\"" + strRef.value() + "\"";
        }
        if (value instanceof ArrayReference arrayRef) {
            List<Value> values = arrayRef.getValues();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < values.size(); i++) {
                sb.append(formatValue(values.get(i)));
                if (i < values.size() - 1) sb.append(", ");
            }
            sb.append("]");
            return sb.toString();
        }
        if (value instanceof ObjectReference objRef) {
            return objRef.referenceType().name() + "@" + objRef.uniqueID();
        }
        return value.toString();
    }

    private String getOrCreateAddress(ObjectReference objRef) {
        String key = String.valueOf(objRef.uniqueID());
        return objectMemoryAddressMap.computeIfAbsent(key, k -> "0x" + Integer.toHexString(1000000 + random.nextInt(9000000)));
    }
}
