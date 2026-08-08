package com.dsa.studio.repository;

import com.dsa.studio.model.PracticeProblem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PracticeProblemRepository extends JpaRepository<PracticeProblem, Long> {
    List<PracticeProblem> findByCategory(String category);
}
