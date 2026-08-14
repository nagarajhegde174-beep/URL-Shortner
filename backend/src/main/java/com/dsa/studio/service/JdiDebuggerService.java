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
        StackFrame topFrame = null;

        try {
            List<StackFrame> frames = thread.frames();
            if (!frames.isEmpty()) {
                topFrame = frames.get(0);
            }
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

        StepMetadata metadata = extractMetadata(className, codeLine, variables, topFrame);

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

    private StepMetadata extractMetadata(String className, String codeLine, List<VariableInfo> variables, StackFrame topFrame) {
        // ── 1. Determine data structure from class name (explicit, not inferred) ──
        String lowerClassName = className.toLowerCase();
        String dataStructure;
        if (lowerClassName.contains("linkedlist") || lowerClassName.contains("node")
                || lowerClassName.contains("singlylist") || lowerClassName.contains("doublylist")
                || lowerClassName.contains("circularlist") || lowerClassName.contains("listnode")) {
            dataStructure = "LINKED_LIST";
        } else if (lowerClassName.contains("string") || lowerClassName.contains("palindrome")
                || lowerClassName.contains("anagram") || lowerClassName.contains("kmp")
                || lowerClassName.contains("rabin") || lowerClassName.contains("naive")
                || lowerClassName.contains("lps") || lowerClassName.contains("pattern")
                || lowerClassName.contains("substring") || lowerClassName.contains("reverse")
                || lowerClassName.contains("charfreq") || lowerClassName.contains("rotation")) {
            dataStructure = "STRING";
        } else {
            dataStructure = "ARRAY";
        }

        String trimmedLine = (codeLine != null) ? codeLine.trim() : "";

        // ── 2. Shared pointer extraction ──────────────────────────────────────────
        Map<String, Integer> pointers = new HashMap<>();
        List<String> intPointerNames = List.of(
            "i", "j", "left", "right", "low", "high", "mid", "start", "end",
            "p1", "p2", "windowStart", "windowEnd", "minIdx", "maxIdx",
            "slow", "fast", "prev", "curr", "k", "n", "m", "patternIdx", "textIdx",
            "lpsIdx", "hashValue", "patternOffset"
        );
        for (VariableInfo v : variables) {
            if (intPointerNames.contains(v.getName())) {
                try {
                    pointers.put(v.getName(), Integer.parseInt(v.getValue()));
                } catch (NumberFormatException ignored) {}
            }
        }

        // ── 3. Active array/string indices from bracket access on current line ────
        List<Integer> indices = new ArrayList<>();
        if (!trimmedLine.isEmpty()) {
            java.util.regex.Pattern bracketPattern = java.util.regex.Pattern.compile("\\[([a-zA-Z0-9_]+)\\]");
            java.util.regex.Matcher bracketMatcher = bracketPattern.matcher(trimmedLine);
            while (bracketMatcher.find()) {
                String idxToken = bracketMatcher.group(1);
                if (idxToken.matches("\\d+")) {
                    indices.add(Integer.parseInt(idxToken));
                } else {
                    for (VariableInfo v : variables) {
                        if (v.getName().equals(idxToken)) {
                            try { indices.add(Integer.parseInt(v.getValue())); } catch (NumberFormatException ignored) {}
                            break;
                        }
                    }
                }
            }
        }

        // ══════════════════════════════════════════════════════════════════════════
        // LINKED LIST METADATA
        // ══════════════════════════════════════════════════════════════════════════
        if ("LINKED_LIST".equals(dataStructure)) {
            return extractLinkedListMetadata(trimmedLine, variables, pointers, indices, topFrame);
        }

        // ══════════════════════════════════════════════════════════════════════════
        // STRING METADATA
        // ══════════════════════════════════════════════════════════════════════════
        if ("STRING".equals(dataStructure)) {
            return extractStringMetadata(trimmedLine, variables, pointers, indices, className);
        }

        // ══════════════════════════════════════════════════════════════════════════
        // ARRAY METADATA (original logic, preserved)
        // ══════════════════════════════════════════════════════════════════════════
        String operation = "READ";
        if (!trimmedLine.isEmpty()) {
            if ((trimmedLine.contains("swap") || lowerClassName.contains("reverse") || lowerClassName.contains("rotate"))
                    && trimmedLine.contains("arr[") && trimmedLine.contains("=") && !trimmedLine.contains("==")) {
                operation = "SWAP";
            }
            if ("READ".equals(operation)) {
                if (trimmedLine.contains("==") || trimmedLine.contains("!=")
                        || (trimmedLine.contains("<") && !trimmedLine.contains("<<"))
                        || (trimmedLine.contains(">") && !trimmedLine.contains(">>"))) {
                    operation = "COMPARE";
                } else if (trimmedLine.matches(".*\\b(i|j|left|right|low|high|mid|start|end|slow|fast)\\s*(\\+\\+|--|\\+=|-=|=).*")) {
                    operation = "POINTER_MOVE";
                } else if (trimmedLine.contains("windowSum") || trimmedLine.contains("windowEnd") || trimmedLine.contains("windowStart")) {
                    operation = "WINDOW_UPDATE";
                } else if (trimmedLine.contains("arr[") && trimmedLine.contains("=") && !trimmedLine.contains("==")) {
                    int eqIdx = trimmedLine.indexOf('=');
                    int arrIdx = trimmedLine.indexOf("arr[");
                    if (arrIdx != -1 && arrIdx < eqIdx) operation = "WRITE";
                }
            }
        }

        return StepMetadata.builder()
                .dataStructure("ARRAY")
                .operation(operation)
                .indices(indices)
                .pointers(pointers)
                .build();
    }

    private StepMetadata extractLinkedListMetadata(String trimmedLine, List<VariableInfo> variables,
                                                   Map<String, Integer> pointers, List<Integer> indices,
                                                   StackFrame topFrame) {
        // ── Operation detection (explicit patterns first) ─────────────────────────
        String operation = "TRAVERSE";
        String nodeId = null;
        Long objectId = null;
        String nextNodeId = null;
        String previousNodeId = null;
        Map<String, String> nodePointers = new HashMap<>();
        List<Map<String, String>> nodeSnapshot = new ArrayList<>();

        if (trimmedLine.contains("new Node") || trimmedLine.contains("new ListNode")
                || trimmedLine.contains("new SinglyNode") || trimmedLine.contains("new DoublyNode")) {
            operation = "NODE_CREATE";
        } else if (trimmedLine.contains(".next = null") || trimmedLine.contains("= null")) {
            operation = "NODE_DELETE";
        } else if (trimmedLine.contains(".next =") || trimmedLine.contains(".prev =")) {
            operation = "POINTER_UPDATE";
        } else if (trimmedLine.contains("head =") || trimmedLine.contains("tail =")
                || trimmedLine.contains("current =") || trimmedLine.contains("curr =")) {
            operation = "POINTER_UPDATE";
        } else if (trimmedLine.contains("==") || trimmedLine.contains("!=")) {
            operation = "COMPARE";
        } else if (trimmedLine.contains("System.out")) {
            operation = "TRAVERSE";
        }

        // ── Extract node reference variables (objects with address) ───────────────
        List<String> nodePointerNames = List.of(
            "head", "tail", "current", "curr", "prev", "next", "slow", "fast",
            "temp", "node", "p", "q", "dummy", "newNode", "result", "merged"
        );
        for (VariableInfo v : variables) {
            if (nodePointerNames.contains(v.getName()) && v.getValue() != null
                    && !v.getValue().equals("null") && v.getMemoryAddress() != null) {
                nodePointers.put(v.getName(), v.getMemoryAddress());
            }
        }

        // Set primary nodeId from the active pointer
        for (String pName : List.of("current", "curr", "head")) {
            VariableInfo pv = variables.stream().filter(v -> v.getName().equals(pName)).findFirst().orElse(null);
            if (pv != null && pv.getMemoryAddress() != null && !pv.getValue().equals("null")) {
                nodeId = pv.getMemoryAddress();
                break;
            }
        }

        // ── Build node snapshot and extract objectId, nextNodeId, prevNodeId ──────
        if (topFrame != null) {
            nodeSnapshot = buildNodeSnapshot(topFrame);
            
            try {
                for (LocalVariable var : topFrame.visibleVariables()) {
                    Value val = topFrame.getValue(var);
                    if (val instanceof ObjectReference objRef) {
                        String addr = getOrCreateAddress(objRef);
                        if (addr.equals(nodeId)) {
                            objectId = objRef.uniqueID();
                            ReferenceType refType = objRef.referenceType();
                            Field nextField = refType.fieldByName("next");
                            Field prevField = refType.fieldByName("prev");
                            if (nextField != null) {
                                Value nextVal = objRef.getValue(nextField);
                                if (nextVal instanceof ObjectReference nextObj) {
                                    nextNodeId = getOrCreateAddress(nextObj);
                                }
                            }
                            if (prevField != null) {
                                Value prevVal = objRef.getValue(prevField);
                                if (prevVal instanceof ObjectReference prevObj) {
                                    previousNodeId = getOrCreateAddress(prevObj);
                                }
                            }
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Error extracting active node details: {}", e.getMessage());
            }
        }

        return StepMetadata.builder()
                .dataStructure("LINKED_LIST")
                .operation(operation)
                .indices(indices)
                .pointers(pointers)
                .nodeId(nodeId)
                .objectId(objectId)
                .nextNodeId(nextNodeId)
                .previousNodeId(previousNodeId)
                .nodePointers(nodePointers)
                .nodeSnapshot(nodeSnapshot)
                .build();
    }

    private List<Map<String, String>> buildNodeSnapshot(StackFrame frame) {
        List<Map<String, String>> snapshot = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        List<ObjectReference> nodesToTraverse = new ArrayList<>();

        try {
            List<LocalVariable> localVars = frame.visibleVariables();
            for (LocalVariable var : localVars) {
                Value val = frame.getValue(var);
                if (val instanceof ObjectReference objRef) {
                    String typeName = objRef.referenceType().name();
                    if (typeName.contains("Node")) {
                        nodesToTraverse.add(objRef);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error finding local node variables for snapshot: {}", e.getMessage());
        }

        int idx = 0;
        while (idx < nodesToTraverse.size()) {
            ObjectReference node = nodesToTraverse.get(idx++);
            long id = node.uniqueID();
            if (visited.contains(id)) {
                continue;
            }
            visited.add(id);

            Map<String, String> nodeData = new HashMap<>();
            String nodeIdStr = getOrCreateAddress(node);
            nodeData.put("nodeId", nodeIdStr);

            ReferenceType refType = node.referenceType();
            Field dataField = refType.fieldByName("data");
            Field nextField = refType.fieldByName("next");
            Field prevField = refType.fieldByName("prev");

            String valStr = "null";
            if (dataField != null) {
                Value dataVal = node.getValue(dataField);
                if (dataVal != null) {
                    valStr = dataVal.toString();
                }
            }
            nodeData.put("data", valStr);

            String nextIdStr = null;
            if (nextField != null) {
                Value nextVal = node.getValue(nextField);
                if (nextVal instanceof ObjectReference nextObj) {
                    nextIdStr = getOrCreateAddress(nextObj);
                    if (!visited.contains(nextObj.uniqueID())) {
                        nodesToTraverse.add(nextObj);
                    }
                }
            }
            nodeData.put("nextNodeId", nextIdStr);

            String prevIdStr = null;
            if (prevField != null) {
                Value prevVal = node.getValue(prevField);
                if (prevVal instanceof ObjectReference prevObj) {
                    prevIdStr = getOrCreateAddress(prevObj);
                    if (!visited.contains(prevObj.uniqueID())) {
                        nodesToTraverse.add(prevObj);
                    }
                }
            }
            nodeData.put("prevNodeId", prevIdStr);

            snapshot.add(nodeData);
        }
        return snapshot;
    }

    private StepMetadata extractStringMetadata(String trimmedLine, List<VariableInfo> variables,
                                               Map<String, Integer> pointers, List<Integer> indices,
                                               String className) {
        // ── Operation detection (explicit string patterns first) ──────────────────
        String operation = "READ";

        if (trimmedLine.contains("charAt(") || trimmedLine.contains("s[") || trimmedLine.contains("str[")) {
            operation = "READ";
        }
        if (trimmedLine.contains("==") || trimmedLine.contains("equals(") || trimmedLine.contains("compareTo(")
                || trimmedLine.contains("charAt") && (trimmedLine.contains("<") || trimmedLine.contains(">"))) {
            operation = "COMPARE";
        }
        // Pattern matching specific
        if (trimmedLine.contains("found = true") || trimmedLine.contains("matched = true")
                || trimmedLine.contains("return true") && className.toLowerCase().contains("palindrome")) {
            operation = "MATCH";
        }
        if (trimmedLine.contains("found = false") || trimmedLine.contains("matched = false")
                || trimmedLine.contains("mismatch")) {
            operation = "MISMATCH";
        }
        if (trimmedLine.matches(".*\\b(i|j|left|right|patternIdx|textIdx|lpsIdx|windowStart|windowEnd)\\s*(\\+\\+|--|\\+=|-=|=).*")) {
            operation = "POINTER_MOVE";
        }
        if (trimmedLine.contains("windowSum") || trimmedLine.contains("maxLen") || trimmedLine.contains("windowEnd")
                || trimmedLine.contains("windowStart")) {
            operation = "WINDOW_UPDATE";
        }
        if (trimmedLine.contains("patternShift") || trimmedLine.contains("shift")
                || (trimmedLine.contains("j =") && className.toLowerCase().contains("kmp"))) {
            operation = "PATTERN_SHIFT";
        }
        if (trimmedLine.contains("hash") || trimmedLine.contains("rollingHash") || trimmedLine.contains("hashCode")) {
            operation = "HASH_COMPUTE";
        }

        // ── Character states: map index → state token ─────────────────────────────
        Map<Integer, String> characterStates = new HashMap<>();
        // Populate active indices as COMPARE state when we're doing comparisons
        for (Integer idx : indices) {
            if ("COMPARE".equals(operation)) {
                characterStates.put(idx, "COMPARE");
            } else if ("MATCH".equals(operation)) {
                characterStates.put(idx, "MATCH");
            } else if ("MISMATCH".equals(operation)) {
                characterStates.put(idx, "MISMATCH");
            } else if ("WINDOW_UPDATE".equals(operation)) {
                characterStates.put(idx, "WINDOW");
            } else {
                characterStates.put(idx, "ACTIVE");
            }
        }

        // ── Pattern and offset from variables ────────────────────────────────────
        String pattern = null;
        Integer patternOffset = null;
        Long rollingHash = null;
        List<Integer> lpsArray = null;

        for (VariableInfo v : variables) {
            switch (v.getName()) {
                case "pattern", "pat" -> pattern = v.getValue();
                case "patternOffset", "shift", "textIdx" -> {
                    try { patternOffset = Integer.parseInt(v.getValue()); } catch (NumberFormatException ignored) {}
                }
                case "rollingHash", "hash", "hashValue", "textHash" -> {
                    try { rollingHash = Long.parseLong(v.getValue()); } catch (NumberFormatException ignored) {}
                }
            }
        }

        // LPS array — detect variable named lps that serializes as [n1, n2, ...]
        for (VariableInfo v : variables) {
            if ("lps".equals(v.getName()) && v.getValue() != null && v.getValue().startsWith("[")) {
                try {
                    String raw = v.getValue().replaceAll("[\\[\\]\\s]", "");
                    if (!raw.isEmpty()) {
                        lpsArray = new ArrayList<>();
                        for (String s : raw.split(",")) {
                            lpsArray.add(Integer.parseInt(s.trim()));
                        }
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        // ── Window boundaries from pointer vars ───────────────────────────────────
        if (pointers.containsKey("windowStart") && pointers.containsKey("windowEnd")) {
            int ws = pointers.get("windowStart");
            int we = pointers.get("windowEnd");
            for (int k = ws; k <= we; k++) {
                characterStates.put(k, "WINDOW");
            }
        }

        return StepMetadata.builder()
                .dataStructure("STRING")
                .operation(operation)
                .indices(indices)
                .pointers(pointers)
                .characterStates(characterStates)
                .lpsArray(lpsArray)
                .pattern(pattern)
                .patternOffset(patternOffset)
                .rollingHash(rollingHash)
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
