package com.dsa.studio.repository;

import com.dsa.studio.model.LearningProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LearningProgressRepository extends JpaRepository<LearningProgress, Long> {
    List<LearningProgress> findByUserId(Long userId);
    Optional<LearningProgress> findByUserIdAndAlgorithmId(Long userId, Long algorithmId);
}
