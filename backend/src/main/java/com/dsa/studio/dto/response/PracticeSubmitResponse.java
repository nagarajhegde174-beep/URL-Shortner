package com.dsa.studio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PracticeSubmitResponse {
    private boolean success;
    private boolean solved;
    private String feedback;
    private String output;
    private String compileError;
    private PracticeProgressResponse progress;
}
