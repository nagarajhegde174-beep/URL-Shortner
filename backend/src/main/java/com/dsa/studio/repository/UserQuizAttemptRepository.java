package com.dsa.studio.repository;

import com.dsa.studio.model.UserQuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserQuizAttemptRepository extends JpaRepository<UserQuizAttempt, Long> {
    List<UserQuizAttempt> findByUserId(Long userId);
    List<UserQuizAttempt> findByUserIdAndCategory(Long userId, String category);
}
