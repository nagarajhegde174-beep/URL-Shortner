package com.dsa.studio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StepDebugInfo {
    private int stepNumber;
    private int lineNumber;
    private List<StackFrameInfo> callStack;
    private List<VariableInfo> variables;
    private String output;
    private String exceptionName;
    private String exceptionMessage;
    private String explanation;
    private StepMetadata metadata;
}
