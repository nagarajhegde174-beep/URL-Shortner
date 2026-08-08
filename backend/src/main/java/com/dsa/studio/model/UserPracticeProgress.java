package com.dsa.studio.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_practice_progress",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "problem_id"})
        })
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class UserPracticeProgress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private PracticeProblem practiceProblem;

    @Column(name = "attempts", nullable = false)
    private Integer attempts = 0;

    @Column(name = "successful_attempts", nullable = false)
    private Integer successfulAttempts = 0;

    @Column(name = "is_solved", nullable = false)
    private boolean solved = false;

    @Column(name = "accuracy", nullable = false)
    private Double accuracy = 0.0;
}
