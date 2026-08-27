package com.urlshortener.analytics.repository;

import com.urlshortener.analytics.entity.Click;
import com.urlshortener.url.entity.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface ClickRepository extends JpaRepository<Click, Long> {
    boolean existsByLinkAndIpHashAndClickedAt(Link link, String ipHash, Instant clickedAt);
}
