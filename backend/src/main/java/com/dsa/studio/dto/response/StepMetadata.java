package com.dsa.studio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StepMetadata {
    private String dataStructure;
    private String operation;
    private List<Integer> indices;
    private Map<String, Integer> pointers;
}
