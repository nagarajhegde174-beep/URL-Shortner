package com.dsa.studio.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "learning_progress")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class LearningProgress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "algorithm_id", nullable = false)
    private Algorithm algorithm;

    @Column(name = "completion_percentage")
    private Integer completionPercentage = 0;

    @Column(name = "is_completed")
    private boolean completed = false;

    @Column(name = "time_spent_minutes")
    private Long timeSpentMinutes = 0L;
}
