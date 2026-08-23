package com.urlshortener.url.service;

import com.urlshortener.common.exception.*;
import com.urlshortener.config.AppProperties;
import com.urlshortener.redis.service.RedisCacheService;
import com.urlshortener.security.CustomUserDetails;
import com.urlshortener.subscription.model.Subscription;
import com.urlshortener.subscription.model.SubscriptionStatus;
import com.urlshortener.subscription.repository.SubscriptionRepository;
import com.urlshortener.url.dto.CreateUrlRequest;
import com.urlshortener.url.dto.UpdateUrlRequest;
import com.urlshortener.url.dto.UrlResponse;
import com.urlshortener.url.entity.Link;
import com.urlshortener.url.repository.LinkRepository;
import com.urlshortener.user.model.Plan;
import com.urlshortener.user.model.Role;
import com.urlshortener.user.model.User;
import com.urlshortener.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UrlServiceTest {

    @Mock
    private LinkRepository linkRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private Base62Service base62Service;
    @Mock
    private RedisCacheService redisCacheService;
    @Mock
    private AppProperties appProperties;

    @InjectMocks
    private UrlService urlService;

    private User freeUser;
    private User proUser;
    private CustomUserDetails freeUserDetails;
    private CustomUserDetails proUserDetails;

    @BeforeEach
    public void setUp() {
        freeUser = User.builder()
                .id(1L)
                .email("free@example.com")
                .passwordHash("hash")
                .role(Role.USER)
                .plan(Plan.FREE)
                .build();

        proUser = User.builder()
                .id(2L)
                .email("pro@example.com")
                .passwordHash("hash")
                .role(Role.USER)
                .plan(Plan.PRO)
                .build();

        freeUserDetails = new CustomUserDetails(freeUser);
        proUserDetails = new CustomUserDetails(proUser);

        lenient().when(appProperties.getBaseUrl()).thenReturn("http://localhost:8080");
    }

    // 1. Base62 Service Verification
    @Test
    public void testBase62GenerationLengthAndCharacters() {
        Base62Service localBase62Service = new Base62Service();
        for (int i = 0; i < 100; i++) {
            String code = localBase62Service.generateShortCode();
            assertEquals(6, code.length());
            assertTrue(code.matches("^[a-zA-Z0-9]+$"));
        }
    }

    // 2. URL Scheme Validation Tests
    @Test
    public void testCreateUrl_ValidUrl() {
        CreateUrlRequest request = new CreateUrlRequest();
        request.setLongUrl("https://google.com/search?q=test");

        when(subscriptionRepository.findByUserId(freeUser.getId())).thenReturn(Optional.empty());
        when(userRepository.findAndLockById(freeUser.getId())).thenReturn(Optional.of(freeUser));
        when(linkRepository.countByUser(freeUser)).thenReturn(2L);
        when(base62Service.generateShortCode()).thenReturn("abc123");
        when(linkRepository.saveAndFlush(any(Link.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UrlResponse response = urlService.createUrl(request, freeUserDetails);
        assertNotNull(response);
        assertEquals("https://google.com/search?q=test", response.getLongUrl());
        assertEquals("abc123", response.getShortCode());
        assertEquals("http://localhost:8080/abc123", response.getShortUrl());
    }

    @Test
    public void testCreateUrl_InvalidScheme_Javascript() {
        CreateUrlRequest request = new CreateUrlRequest();
        request.setLongUrl("javascript:alert('hack')");

        assertThrows(BadRequestException.class, () -> urlService.createUrl(request, freeUserDetails));
    }

    @Test
    public void testCreateUrl_InvalidScheme_File() {
        CreateUrlRequest request = new CreateUrlRequest();
        request.setLongUrl("file:///etc/passwd");

        assertThrows(BadRequestException.class, () -> urlService.createUrl(request, freeUserDetails));
    }

    @Test
    public void testCreateUrl_InvalidScheme_Ftp() {
        CreateUrlRequest request = new CreateUrlRequest();
        request.setLongUrl("ftp://files.example.com/test");

        assertThrows(BadRequestException.class, () -> urlService.createUrl(request, freeUserDetails));
    }

    // 3. Collision Retry Mechanism Tests
    @Test
    public void testCreateUrl_CollisionRetrySuccess() {
        CreateUrlRequest request = new CreateUrlRequest();
        request.setLongUrl("https://example.com");

        when(subscriptionRepository.findByUserId(freeUser.getId())).thenReturn(Optional.empty());
        when(userRepository.findAndLockById(freeUser.getId())).thenReturn(Optional.of(freeUser));
        when(linkRepository.countByUser(freeUser)).thenReturn(0L);

        // Mock base62 to return different codes
        when(base62Service.generateShortCode())
                .thenReturn("code11")
                .thenReturn("code22");

        // First attempt throws Unique Constraint exception
        when(linkRepository.saveAndFlush(any(Link.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate short_code"))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UrlResponse response = urlService.createUrl(request, freeUserDetails);
        assertNotNull(response);
        assertEquals("code22", response.getShortCode());
        verify(linkRepository, times(2)).saveAndFlush(any(Link.class));
    }

    @Test
    public void testCreateUrl_CollisionRetryFailsAfter5Attempts() {
        CreateUrlRequest request = new CreateUrlRequest();
        request.setLongUrl("https://example.com");

        when(subscriptionRepository.findByUserId(freeUser.getId())).thenReturn(Optional.empty());
        when(userRepository.findAndLockById(freeUser.getId())).thenReturn(Optional.of(freeUser));
        when(linkRepository.countByUser(freeUser)).thenReturn(0L);
        when(base62Service.generateShortCode()).thenReturn("code99");
        when(linkRepository.saveAndFlush(any(Link.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate short_code"));

        assertThrows(ShortCodeGenerationException.class, () -> urlService.createUrl(request, freeUserDetails));
        verify(linkRepository, times(5)).saveAndFlush(any(Link.class));
    }

    // 4. Free Plan URL Exceed Limit Test
    @Test
    public void testCreateUrl_FreePlanLimitExceeded() {
        CreateUrlRequest request = new CreateUrlRequest();
        request.setLongUrl("https://example.com");

        when(subscriptionRepository.findByUserId(freeUser.getId())).thenReturn(Optional.empty());
        when(userRepository.findAndLockById(freeUser.getId())).thenReturn(Optional.of(freeUser));
        when(linkRepository.countByUser(freeUser)).thenReturn(5L);

        assertThrows(FreePlanLimitException.class, () -> urlService.createUrl(request, freeUserDetails));
    }

    // 5. PRO User Unlimited/Custom Code Behavior
    @Test
    public void testCreateUrl_ProUserUnlimited() {
        CreateUrlRequest request = new CreateUrlRequest();
        request.setLongUrl("https://example.com");

        Subscription activeSub = Subscription.builder()
                .user(proUser)
                .plan(Plan.PRO)
                .status(SubscriptionStatus.ACTIVE)
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        when(subscriptionRepository.findByUserId(proUser.getId())).thenReturn(Optional.of(activeSub));
        when(base62Service.generateShortCode()).thenReturn("pro123");
        when(linkRepository.saveAndFlush(any(Link.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Pro user has 10 URLs already, but can still create more
        UrlResponse response = urlService.createUrl(request, proUserDetails);
        assertNotNull(response);
        assertEquals("pro123", response.getShortCode());
        verify(userRepository, never()).findAndLockById(anyLong());
    }

    @Test
    public void testCreateUrl_FreeUserCustomCodeForbidden() {
        CreateUrlRequest request = new CreateUrlRequest();
        request.setLongUrl("https://example.com");
        request.setCustomCode("mycode");

        when(subscriptionRepository.findByUserId(freeUser.getId())).thenReturn(Optional.empty());

        assertThrows(SubscriptionExpiredException.class, () -> urlService.createUrl(request, freeUserDetails));
    }

    @Test
    public void testCreateUrl_ProUserCustomCodeSuccess() {
        CreateUrlRequest request = new CreateUrlRequest();
        request.setLongUrl("https://example.com");
        request.setCustomCode("my-custom_123");

        Subscription activeSub = Subscription.builder()
                .user(proUser)
                .plan(Plan.PRO)
                .status(SubscriptionStatus.ACTIVE)
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        when(subscriptionRepository.findByUserId(proUser.getId())).thenReturn(Optional.of(activeSub));
        when(linkRepository.existsByShortCode("my-custom_123")).thenReturn(false);
        when(linkRepository.save(any(Link.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UrlResponse response = urlService.createUrl(request, proUserDetails);
        assertNotNull(response);
        assertEquals("my-custom_123", response.getShortCode());
        assertTrue(response.isCustom());
    }

    @Test
    public void testCreateUrl_ProUserCustomCodeInvalidFormat() {
        CreateUrlRequest request = new CreateUrlRequest();
        request.setLongUrl("https://example.com");
        request.setCustomCode("ab"); // < 3 chars

        Subscription activeSub = Subscription.builder()
                .user(proUser)
                .plan(Plan.PRO)
                .status(SubscriptionStatus.ACTIVE)
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        when(subscriptionRepository.findByUserId(proUser.getId())).thenReturn(Optional.of(activeSub));

        assertThrows(InvalidShortCodeException.class, () -> urlService.createUrl(request, proUserDetails));
    }

    @Test
    public void testCreateUrl_ProUserCustomCodeReservedWord() {
        CreateUrlRequest request = new CreateUrlRequest();
        request.setLongUrl("https://example.com");
        request.setCustomCode("swagger");

        Subscription activeSub = Subscription.builder()
                .user(proUser)
                .plan(Plan.PRO)
                .status(SubscriptionStatus.ACTIVE)
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        when(subscriptionRepository.findByUserId(proUser.getId())).thenReturn(Optional.of(activeSub));

        assertThrows(InvalidShortCodeException.class, () -> urlService.createUrl(request, proUserDetails));
    }

    // 6. Ownership & Authorization Tests
    @Test
    public void testUpdateUrl_UnauthorizedUser() {
        UpdateUrlRequest request = new UpdateUrlRequest();
        request.setLongUrl("https://updated.com");

        Link linkOwnedByPro = Link.builder()
                .id(100L)
                .user(proUser)
                .shortCode("code1")
                .longUrl("https://old.com")
                .build();

        when(linkRepository.findById(100L)).thenReturn(Optional.of(linkOwnedByPro));

        // Free user attempts to update Pro user's link
        assertThrows(ResourceNotFoundException.class, () -> urlService.updateUrl(100L, request, freeUserDetails));
    }

    @Test
    public void testDeleteUrl_UnauthorizedUser() {
        Link linkOwnedByFree = Link.builder()
                .id(200L)
                .user(freeUser)
                .shortCode("code2")
                .longUrl("https://example.com")
                .build();

        when(linkRepository.findById(200L)).thenReturn(Optional.of(linkOwnedByFree));

        // Pro user attempts to delete Free user's link
        assertThrows(ResourceNotFoundException.class, () -> urlService.deleteUrl(200L, proUserDetails));
    }

    // 7. Cache Invalidation Verification
    @Test
    public void testUpdateUrl_EvictsCache() {
        UpdateUrlRequest request = new UpdateUrlRequest();
        request.setLongUrl("https://updated.com");
        request.setIsActive(true);

        Link link = Link.builder()
                .id(50L)
                .user(freeUser)
                .shortCode("evict1")
                .longUrl("https://old.com")
                .build();

        when(linkRepository.findById(50L)).thenReturn(Optional.of(link));
        when(linkRepository.save(any(Link.class))).thenAnswer(invocation -> invocation.getArgument(0));

        urlService.updateUrl(50L, request, freeUserDetails);

        verify(redisCacheService).evictUrl("evict1");
    }
}
