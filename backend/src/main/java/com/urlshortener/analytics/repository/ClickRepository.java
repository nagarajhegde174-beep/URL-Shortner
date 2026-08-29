package com.urlshortener.analytics.repository;

import com.urlshortener.analytics.entity.Click;
import com.urlshortener.url.entity.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Repository
public interface ClickRepository extends JpaRepository<Click, Long> {
    boolean existsByLinkAndIpHashAndClickedAt(Link link, String ipHash, Instant clickedAt);

    long countByLinkId(Long linkId);

    @Query("SELECT COUNT(c) FROM Click c WHERE c.link.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT c.link.id, COUNT(c) FROM Click c WHERE c.link.id IN :linkIds GROUP BY c.link.id")
    List<Object[]> countByLinkIds(@Param("linkIds") Collection<Long> linkIds);

    @Query(value = """
            SELECT DATE(clicked_at) AS click_date, COUNT(*) AS click_count
            FROM clicks
            WHERE link_id = :linkId
            GROUP BY DATE(clicked_at)
            ORDER BY click_date
            """, nativeQuery = true)
    List<Object[]> countClicksByDate(@Param("linkId") Long linkId);

    @Query(value = """
            SELECT COALESCE(NULLIF(device_type, ''), 'Unknown') AS label, COUNT(*) AS click_count
            FROM clicks
            WHERE link_id = :linkId
            GROUP BY COALESCE(NULLIF(device_type, ''), 'Unknown')
            ORDER BY click_count DESC
            """, nativeQuery = true)
    List<Object[]> countByDeviceType(@Param("linkId") Long linkId);

    @Query(value = """
            SELECT COALESCE(NULLIF(browser, ''), 'Unknown') AS label, COUNT(*) AS click_count
            FROM clicks
            WHERE link_id = :linkId
            GROUP BY COALESCE(NULLIF(browser, ''), 'Unknown')
            ORDER BY click_count DESC
            """, nativeQuery = true)
    List<Object[]> countByBrowser(@Param("linkId") Long linkId);

    @Query(value = """
            SELECT COALESCE(NULLIF(operating_system, ''), 'Unknown') AS label, COUNT(*) AS click_count
            FROM clicks
            WHERE link_id = :linkId
            GROUP BY COALESCE(NULLIF(operating_system, ''), 'Unknown')
            ORDER BY click_count DESC
            """, nativeQuery = true)
    List<Object[]> countByOperatingSystem(@Param("linkId") Long linkId);

    @Query(value = """
            SELECT COALESCE(NULLIF(referrer, ''), 'Direct') AS label, COUNT(*) AS click_count
            FROM clicks
            WHERE link_id = :linkId
            GROUP BY COALESCE(NULLIF(referrer, ''), 'Direct')
            ORDER BY click_count DESC
            LIMIT 10
            """, nativeQuery = true)
    List<Object[]> countByReferrer(@Param("linkId") Long linkId);
}
