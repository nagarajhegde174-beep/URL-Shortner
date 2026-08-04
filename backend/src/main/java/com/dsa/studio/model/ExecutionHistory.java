package com.dsa.studio.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "execution_history")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ExecutionHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "java_code", columnDefinition = "TEXT", nullable = false)
    private String javaCode;

    @Column(columnDefinition = "TEXT")
    private String output;

    @Column(name = "execution_status")
    private String executionStatus;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;
}
