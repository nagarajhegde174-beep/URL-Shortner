package com.urlshortener.url.repository;

import com.urlshortener.url.entity.Link;
import com.urlshortener.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LinkRepository extends JpaRepository<Link, Long> {
    Optional<Link> findByShortCode(String shortCode);
    Page<Link> findByUser(User user, Pageable pageable);
    long countByUser(User user);
    boolean existsByShortCode(String shortCode);
}
