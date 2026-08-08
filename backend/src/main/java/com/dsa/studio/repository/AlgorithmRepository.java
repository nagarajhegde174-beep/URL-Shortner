package com.dsa.studio.repository;

import com.dsa.studio.model.Algorithm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlgorithmRepository extends JpaRepository<Algorithm, Long> {
    List<Algorithm> findByCategory(String category);
    Optional<Algorithm> findByCategoryAndName(String category, String name);
}
