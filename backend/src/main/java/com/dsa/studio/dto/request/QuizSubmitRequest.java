package com.dsa.studio.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizSubmitRequest {
    private String quizTitle;
    private String category;
    private Integer score;
    private Integer totalQuestions;
}
