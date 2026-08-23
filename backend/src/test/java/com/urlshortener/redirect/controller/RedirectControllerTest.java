package com.urlshortener.redirect.controller;

import com.urlshortener.common.exception.ResourceGoneException;
import com.urlshortener.common.exception.ResourceNotFoundException;
import com.urlshortener.redis.service.RedisCacheService;
import com.urlshortener.url.dto.CachedUrl;
import com.urlshortener.url.entity.Link;
import com.urlshortener.url.repository.LinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RedirectControllerTest {

    @Mock
    private LinkRepository linkRepository;
    @Mock
    private RedisCacheService redisCacheService;

    @InjectMocks
    private RedirectController redirectController;

    private CachedUrl activeCachedUrl;
    private Link activeDbLink;

    @BeforeEach
    public void setUp() {
        activeCachedUrl = CachedUrl.builder()
                .longUrl("https://example.com/target")
                .isActive(true)
                .expiresAt(null)
                .build();

        activeDbLink = Link.builder()
                .id(1L)
                .shortCode("xyz123")
                .longUrl("https://example.com/target")
                .isActive(true)
                .expiresAt(null)
                .build();
    }

    // 1. Cache HIT Tests
    @Test
    public void testRedirect_CacheHit_Success302() {
        when(redisCacheService.getCachedUrl("xyz123")).thenReturn(activeCachedUrl);

        ResponseEntity<Void> response = redirectController.redirect("xyz123");
        assertNotNull(response);
        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals(URI.create("https://example.com/target"), response.getHeaders().getLocation());
        verifyNoInteractions(linkRepository);
    }

    @Test
    public void testRedirect_CacheHit_Disabled404() {
        activeCachedUrl.setActive(false);
        when(redisCacheService.getCachedUrl("xyz123")).thenReturn(activeCachedUrl);

        assertThrows(ResourceNotFoundException.class, () -> redirectController.redirect("xyz123"));
        verifyNoInteractions(linkRepository);
    }

    @Test
    public void testRedirect_CacheHit_Expired410() {
        activeCachedUrl.setExpiresAt(Instant.now().minusSeconds(10));
        when(redisCacheService.getCachedUrl("xyz123")).thenReturn(activeCachedUrl);

        assertThrows(ResourceGoneException.class, () -> redirectController.redirect("xyz123"));
        verifyNoInteractions(linkRepository);
    }

    // 2. Cache MISS Tests
    @Test
    public void testRedirect_CacheMiss_Success302() {
        when(redisCacheService.getCachedUrl("xyz123")).thenReturn(null);
        when(linkRepository.findByShortCode("xyz123")).thenReturn(Optional.of(activeDbLink));

        ResponseEntity<Void> response = redirectController.redirect("xyz123");
        assertNotNull(response);
        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals(URI.create("https://example.com/target"), response.getHeaders().getLocation());

        // Verify it updates cache with valid entity
        verify(redisCacheService).cacheUrl(eq("xyz123"), any(CachedUrl.class));
    }

    @Test
    public void testRedirect_CacheMiss_Unknown404() {
        when(redisCacheService.getCachedUrl("xyz123")).thenReturn(null);
        when(linkRepository.findByShortCode("xyz123")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> redirectController.redirect("xyz123"));
        verify(redisCacheService, never()).cacheUrl(any(), any());
    }

    @Test
    public void testRedirect_CacheMiss_Disabled404() {
        activeDbLink.setActive(false);
        when(redisCacheService.getCachedUrl("xyz123")).thenReturn(null);
        when(linkRepository.findByShortCode("xyz123")).thenReturn(Optional.of(activeDbLink));

        assertThrows(ResourceNotFoundException.class, () -> redirectController.redirect("xyz123"));
        verify(redisCacheService, never()).cacheUrl(any(), any());
    }

    @Test
    public void testRedirect_CacheMiss_Expired410() {
        activeDbLink.setExpiresAt(Instant.now().minusSeconds(10));
        when(redisCacheService.getCachedUrl("xyz123")).thenReturn(null);
        when(linkRepository.findByShortCode("xyz123")).thenReturn(Optional.of(activeDbLink));

        assertThrows(ResourceGoneException.class, () -> redirectController.redirect("xyz123"));
        verify(redisCacheService, never()).cacheUrl(any(), any());
    }
}
