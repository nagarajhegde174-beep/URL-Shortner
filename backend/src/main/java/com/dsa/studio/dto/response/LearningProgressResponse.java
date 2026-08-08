package com.dsa.studio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningProgressResponse {
    private Long algorithmId;
    private String name;
    private String category;
    private Integer completionPercentage;
    private boolean completed;
}
