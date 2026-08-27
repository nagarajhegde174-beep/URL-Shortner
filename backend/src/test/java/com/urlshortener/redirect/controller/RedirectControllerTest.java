package com.urlshortener.redirect.controller;

import com.urlshortener.analytics.dto.ClickEventDto;
import com.urlshortener.common.exception.ResourceGoneException;
import com.urlshortener.common.exception.ResourceNotFoundException;
import com.urlshortener.kafka.producer.ClickEventProducer;
import com.urlshortener.redis.service.RedisCacheService;
import com.urlshortener.url.dto.CachedUrl;
import com.urlshortener.url.entity.Link;
import com.urlshortener.url.repository.LinkRepository;
import jakarta.servlet.http.HttpServletRequest;
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
    @Mock
    private ClickEventProducer clickEventProducer;
    @Mock
    private HttpServletRequest request;

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
    public void testRedirect_CacheHit_Success302_PublishesEvent() {
        when(redisCacheService.getCachedUrl("xyz123")).thenReturn(activeCachedUrl);
        when(linkRepository.findByShortCode("xyz123")).thenReturn(Optional.of(activeDbLink));
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        ResponseEntity<Void> response = redirectController.redirect("xyz123");
        assertNotNull(response);
        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals(URI.create("https://example.com/target"), response.getHeaders().getLocation());
        
        // Verifies click event published
        verify(clickEventProducer).publishClickEvent(any(ClickEventDto.class));
    }

    @Test
    public void testRedirect_CacheHit_Disabled404_NoEvent() {
        activeCachedUrl.setActive(false);
        when(redisCacheService.getCachedUrl("xyz123")).thenReturn(activeCachedUrl);

        assertThrows(ResourceNotFoundException.class, () -> redirectController.redirect("xyz123"));
        verifyNoInteractions(clickEventProducer);
    }

    @Test
    public void testRedirect_CacheHit_Expired410_NoEvent() {
        activeCachedUrl.setExpiresAt(Instant.now().minusSeconds(10));
        when(redisCacheService.getCachedUrl("xyz123")).thenReturn(activeCachedUrl);

        assertThrows(ResourceGoneException.class, () -> redirectController.redirect("xyz123"));
        verifyNoInteractions(clickEventProducer);
    }

    // 2. Cache MISS Tests
    @Test
    public void testRedirect_CacheMiss_Success302_PublishesEvent() {
        when(redisCacheService.getCachedUrl("xyz123")).thenReturn(null);
        when(linkRepository.findByShortCode("xyz123")).thenReturn(Optional.of(activeDbLink));
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        ResponseEntity<Void> response = redirectController.redirect("xyz123");
        assertNotNull(response);
        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals(URI.create("https://example.com/target"), response.getHeaders().getLocation());

        // Verify cache write and event publish
        verify(redisCacheService).cacheUrl(eq("xyz123"), any(CachedUrl.class));
        verify(clickEventProducer).publishClickEvent(any(ClickEventDto.class));
    }

    @Test
    public void testRedirect_CacheMiss_Unknown404_NoEvent() {
        when(redisCacheService.getCachedUrl("xyz123")).thenReturn(null);
        when(linkRepository.findByShortCode("xyz123")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> redirectController.redirect("xyz123"));
        verifyNoInteractions(clickEventProducer);
    }

    @Test
    public void testRedirect_CacheMiss_Disabled404_NoEvent() {
        activeDbLink.setActive(false);
        when(redisCacheService.getCachedUrl("xyz123")).thenReturn(null);
        when(linkRepository.findByShortCode("xyz123")).thenReturn(Optional.of(activeDbLink));

        assertThrows(ResourceNotFoundException.class, () -> redirectController.redirect("xyz123"));
        verifyNoInteractions(clickEventProducer);
    }

    @Test
    public void testRedirect_CacheMiss_Expired410_NoEvent() {
        activeDbLink.setExpiresAt(Instant.now().minusSeconds(10));
        when(redisCacheService.getCachedUrl("xyz123")).thenReturn(null);
        when(linkRepository.findByShortCode("xyz123")).thenReturn(Optional.of(activeDbLink));

        assertThrows(ResourceGoneException.class, () -> redirectController.redirect("xyz123"));
        verifyNoInteractions(clickEventProducer);
    }
}
