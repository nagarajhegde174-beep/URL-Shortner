package com.dsa.studio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionStartResponse {
    private String sessionId;
    private StepDebugInfo firstStep;
}
