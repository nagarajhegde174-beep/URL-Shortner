package com.dsa.studio.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearningProgressUpdateRequest {
    private Long algorithmId;
    private Integer completionPercentage;
    private boolean completed;
}
