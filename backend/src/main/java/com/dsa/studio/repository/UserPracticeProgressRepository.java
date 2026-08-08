package com.dsa.studio.repository;

import com.dsa.studio.model.UserPracticeProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserPracticeProgressRepository extends JpaRepository<UserPracticeProgress, Long> {
    List<UserPracticeProgress> findByUserId(Long userId);
    Optional<UserPracticeProgress> findByUserIdAndPracticeProblemId(Long userId, Long problemId);
}
