package com.dsa.studio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PracticeProgressResponse {
    private Long problemId;
    private String title;
    private String difficulty;
    private Integer attempts;
    private Integer successfulAttempts;
    private boolean solved;
    private Double accuracy;
}
