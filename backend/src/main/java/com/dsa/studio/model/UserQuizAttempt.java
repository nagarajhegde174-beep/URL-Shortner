package com.dsa.studio.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_quiz_attempts")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class UserQuizAttempt extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String category = "ARRAY";

    @Column(name = "quiz_title", nullable = false)
    private String quizTitle;

    @Column(nullable = false)
    private Integer score = 0;

    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions = 0;

    @Column(nullable = false)
    private Double percentage = 0.0;

    @Column(name = "is_completed", nullable = false)
    private boolean completed = false;

    @Column(name = "attempt_date", nullable = false)
    private LocalDateTime attemptDate = LocalDateTime.now();
}
