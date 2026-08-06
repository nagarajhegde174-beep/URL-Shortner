package com.dsa.studio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariableInfo {
    private String name;
    private String type;
    private String value;
    private String previousValue;
    private String memoryAddress; // Simulated memory address
    private String scope;
    private String lifetime;
    private String status; // "NEW", "UPDATED", "DELETED", "UNCHANGED"
}
