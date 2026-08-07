package com.dsa.studio.service;

import com.dsa.studio.dto.response.StackFrameInfo;
import com.dsa.studio.dto.response.StepDebugInfo;
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
                    else if (event instanceof ClassPrepareEvent) {
                        // Class prepared, start step requests on all threads
                        List<ThreadReference> threads = vm.allThreads();
                        for (ThreadReference thread : threads) {
                            if (thread.status() == ThreadReference.THREAD_STATUS_RUNNING ||
                                thread.status() == ThreadReference.THREAD_STATUS_UNKNOWN) {
                                continue;
                            }
                            createStepRequest(erm, thread);
                        }
                    } 
                    else if (event instanceof StepEvent stepEvent) {
                        Location location = stepEvent.location();
                        String locClassName = location.declaringType().name();

                        // Only record steps within our compiled user class and ignore standard library classes
                        if (locClassName.equals(className)) {
                            stepCounter++;
                            ThreadReference thread = stepEvent.thread();
                            
                            // Capture current state
                            StepDebugInfo debugInfo = captureState(
                                    stepCounter,
                                    location.lineNumber(),
                                    thread,
                                    stdoutBuffer.toString(),
                                    previousVariableValues
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

    private StepDebugInfo captureState(int stepNum, int lineNum, ThreadReference thread, String currentOutput, Map<String, String> prevVals) {
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

        return StepDebugInfo.builder()
                .stepNumber(stepNum)
                .lineNumber(lineNum)
                .callStack(callStack)
                .variables(variables)
                .output(currentOutput)
                .explanation(explanation)
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
