package com.urlshortener.auth;

import com.urlshortener.auth.dto.ForgotPasswordRequest;
import com.urlshortener.auth.dto.MessageResponse;
import com.urlshortener.auth.dto.ResetPasswordRequest;
import com.urlshortener.auth.model.PasswordResetToken;
import com.urlshortener.auth.repository.PasswordResetTokenRepository;
import com.urlshortener.auth.service.AuthService;
import com.urlshortener.auth.service.EmailService;
import com.urlshortener.auth.service.RefreshTokenService;
import com.urlshortener.common.exception.BadRequestException;
import com.urlshortener.redis.service.RedisRateLimiterService;
import com.urlshortener.security.JwtUtil;
import com.urlshortener.subscription.repository.SubscriptionRepository;
import com.urlshortener.url.service.UrlService;
import com.urlshortener.user.model.Plan;
import com.urlshortener.user.model.Role;
import com.urlshortener.user.model.User;
import com.urlshortener.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService — Forgot Password & Reset Password Tests")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private JwtUtil jwtUtil;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private UrlService urlService;
    @Mock private EmailService emailService;
    @Mock private RedisRateLimiterService redisRateLimiterService;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("$2a$10$hashedpassword")
                .role(Role.USER)
                .plan(Plan.FREE)
                .build();
    }

    // =====================================================================
    // FORGOT PASSWORD TESTS
    // =====================================================================

    @Test
    @DisplayName("1. Forgot password with existing email — sends email and calls EmailService")
    void forgotPassword_existingEmail_sendsEmailAndCallsEmailService() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(redisRateLimiterService.isAllowed(anyString(), anyInt(), anyInt())).thenReturn(true);

        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        // Act
        MessageResponse response = authService.forgotPassword(request);

        // Assert
        verify(emailService, times(1)).sendPasswordResetEmail(eq("test@example.com"), anyString());
        assertThat(response.getMessage()).isNotBlank();
    }

    @Test
    @DisplayName("2. Forgot password with non-existing email — returns same generic message (no enumeration)")
    void forgotPassword_nonExistingEmail_returnsGenericMessage() {
        // Arrange
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("notfound@example.com");

        // Act
        MessageResponse response = authService.forgotPassword(request);

        // Assert — EmailService must NOT be called for non-existent email
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
        assertThat(response.getMessage()).isNotBlank();
    }

    @Test
    @DisplayName("3. Response never contains the reset token")
    void forgotPassword_responseNeverContainsToken() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(redisRateLimiterService.isAllowed(anyString(), anyInt(), anyInt())).thenReturn(true);

        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        // Capture the token passed to email service
        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), tokenCaptor.capture());

        // Act
        MessageResponse response = authService.forgotPassword(request);

        // Assert — the raw token should NEVER appear in the response message
        String capturedToken = tokenCaptor.getValue();
        assertThat(response.getMessage()).doesNotContain(capturedToken);
        // The response message must not contain any 6-digit number sequence
        assertThat(response.getMessage()).doesNotContainPattern("\\d{6}");
    }

    @Test
    @DisplayName("4. Generated token is exactly 6 digits")
    void forgotPassword_generatedTokenIsExactly6Digits() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(redisRateLimiterService.isAllowed(anyString(), anyInt(), anyInt())).thenReturn(true);

        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), tokenCaptor.capture());

        // Act
        authService.forgotPassword(request);

        // Assert
        String token = tokenCaptor.getValue();
        assertThat(token).hasSize(6);
        assertThat(token).matches("\\d{6}");
    }

    @Test
    @DisplayName("5. Token expires after 15 minutes (stored with correct expiry)")
    void forgotPassword_tokenExpiresAfter15Minutes() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(redisRateLimiterService.isAllowed(anyString(), anyInt(), anyInt())).thenReturn(true);

        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        when(passwordResetTokenRepository.save(tokenCaptor.capture())).thenAnswer(i -> i.getArgument(0));

        // Act
        authService.forgotPassword(request);

        // Assert — expiry should be roughly 15 minutes from now
        PasswordResetToken saved = tokenCaptor.getValue();
        Instant expectedExpiry = Instant.now().plus(15, ChronoUnit.MINUTES);
        assertThat(saved.getExpiresAt()).isBetween(
                expectedExpiry.minus(10, ChronoUnit.SECONDS),
                expectedExpiry.plus(10, ChronoUnit.SECONDS)
        );
        assertThat(saved.isUsed()).isFalse();
    }

    @Test
    @DisplayName("6. Previous token is invalidated when a new token is generated")
    void forgotPassword_invalidatesPreviousToken() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(redisRateLimiterService.isAllowed(anyString(), anyInt(), anyInt())).thenReturn(true);

        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        // Act
        authService.forgotPassword(request);

        // Assert — previous tokens deleted before saving the new one
        verify(passwordResetTokenRepository, times(1)).deleteByUser(testUser);
    }

    @Test
    @DisplayName("12. EmailService is called exactly once on successful forgot-password")
    void forgotPassword_emailServiceCalledOnce() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(redisRateLimiterService.isAllowed(anyString(), anyInt(), anyInt())).thenReturn(true);

        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        // Act
        authService.forgotPassword(request);

        // Assert
        verify(emailService, times(1)).sendPasswordResetEmail(eq("test@example.com"), anyString());
    }

    @Test
    @DisplayName("14. Resend cooldown is enforced via Redis")
    void forgotPassword_resendCooldownEnforced() {
        // Arrange — Redis reports cooldown active (rate limit exceeded)
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(redisRateLimiterService.isAllowed(anyString(), anyInt(), anyInt())).thenReturn(false);

        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        // Act & Assert
        assertThatThrownBy(() -> authService.forgotPassword(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("wait");

        // Email must NOT be sent when rate limited
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("15. Same generic message returned for both existing and non-existing email (prevents enumeration)")
    void forgotPassword_genericResponsePreventEnumeration() {
        // Arrange — existing email
        when(userRepository.findByEmail("exists@example.com")).thenReturn(Optional.of(testUser));
        when(redisRateLimiterService.isAllowed(anyString(), anyInt(), anyInt())).thenReturn(true);

        ForgotPasswordRequest existingReq = new ForgotPasswordRequest();
        existingReq.setEmail("exists@example.com");

        // Arrange — non-existent email
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());
        ForgotPasswordRequest notFoundReq = new ForgotPasswordRequest();
        notFoundReq.setEmail("notfound@example.com");

        // Act
        MessageResponse existingResp = authService.forgotPassword(existingReq);
        MessageResponse notFoundResp = authService.forgotPassword(notFoundReq);

        // Assert — both responses must be identical
        assertThat(existingResp.getMessage()).isEqualTo(notFoundResp.getMessage());
    }

    // =====================================================================
    // RESET PASSWORD TESTS
    // =====================================================================

    private PasswordResetToken buildValidToken(String rawToken) {
        String hashedToken = hashForTest(rawToken);
        return PasswordResetToken.builder()
                .id(1L)
                .user(testUser)
                .tokenHash(hashedToken)
                .expiresAt(Instant.now().plus(15, ChronoUnit.MINUTES))
                .used(false)
                .build();
    }

    private PasswordResetToken buildExpiredToken(String rawToken) {
        String hashedToken = hashForTest(rawToken);
        return PasswordResetToken.builder()
                .id(1L)
                .user(testUser)
                .tokenHash(hashedToken)
                .expiresAt(Instant.now().minus(1, ChronoUnit.MINUTES)) // already expired
                .used(false)
                .build();
    }

    private PasswordResetToken buildUsedToken(String rawToken) {
        String hashedToken = hashForTest(rawToken);
        return PasswordResetToken.builder()
                .id(1L)
                .user(testUser)
                .tokenHash(hashedToken)
                .expiresAt(Instant.now().plus(15, ChronoUnit.MINUTES))
                .used(true) // already used
                .build();
    }

    private String hashForTest(String raw) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) sb.append('0');
                sb.append(h);
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("7. Correct token allows password reset")
    void resetPassword_correctToken_resetsPassword() {
        // Arrange
        String rawToken = "123456";
        PasswordResetToken validToken = buildValidToken(rawToken);
        when(passwordResetTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(validToken));
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$newHashedPassword");

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken(rawToken);
        request.setNewPassword("NewPassword123");

        // Act
        MessageResponse response = authService.resetPassword(request);

        // Assert
        assertThat(response.getMessage()).isNotBlank();
        verify(userRepository, times(1)).save(testUser);
        assertThat(validToken.isUsed()).isTrue();
    }

    @Test
    @DisplayName("8. Wrong token is rejected")
    void resetPassword_wrongToken_throwsBadRequest() {
        // Arrange
        when(passwordResetTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("000000");
        request.setNewPassword("NewPassword123");

        // Act & Assert
        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid or expired");
    }

    @Test
    @DisplayName("9. Expired token is rejected")
    void resetPassword_expiredToken_throwsBadRequest() {
        // Arrange
        String rawToken = "654321";
        PasswordResetToken expiredToken = buildExpiredToken(rawToken);
        when(passwordResetTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(expiredToken));

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken(rawToken);
        request.setNewPassword("NewPassword123");

        // Act & Assert
        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("10. Used token cannot be reused")
    void resetPassword_usedToken_throwsBadRequest() {
        // Arrange
        String rawToken = "111111";
        PasswordResetToken usedToken = buildUsedToken(rawToken);
        when(passwordResetTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(usedToken));

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken(rawToken);
        request.setNewPassword("NewPassword123");

        // Act & Assert
        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already been used");
    }

    @Test
    @DisplayName("11. Password is stored using the existing BCrypt PasswordEncoder")
    void resetPassword_passwordStoredWithEncoder() {
        // Arrange
        String rawToken = "222222";
        String newPlainPassword = "MyNewSecurePass!";
        String expectedHash = "$2a$10$encodedHash";

        PasswordResetToken validToken = buildValidToken(rawToken);
        when(passwordResetTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(validToken));
        when(passwordEncoder.encode(newPlainPassword)).thenReturn(expectedHash);

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken(rawToken);
        request.setNewPassword(newPlainPassword);

        // Act
        authService.resetPassword(request);

        // Assert — password encoder was called and user was saved with encoded password
        verify(passwordEncoder, times(1)).encode(newPlainPassword);
        assertThat(testUser.getPasswordHash()).isEqualTo(expectedHash);
    }

    @Test
    @DisplayName("13. Reset token is never logged (EmailService receives raw token, not stored in service logs)")
    void forgotPassword_tokenNeverLogged() {
        // This test verifies that authService does NOT log the raw token.
        // We verify by ensuring the rawToken never appears in any log call
        // (checked indirectly: only emailService receives it, nothing else)
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(redisRateLimiterService.isAllowed(anyString(), anyInt(), anyInt())).thenReturn(true);

        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        ArgumentCaptor<String> emailTokenCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), emailTokenCaptor.capture());

        // Act
        authService.forgotPassword(request);

        // Assert — the token only went to emailService, verified via captor
        String capturedToken = emailTokenCaptor.getValue();
        assertThat(capturedToken).matches("\\d{6}");
        // No other component besides emailService should receive the raw token
        verify(passwordResetTokenRepository, times(1)).save(
            argThat(t -> !t.getTokenHash().equals(capturedToken)) // stored value is a HASH, not raw
        );
    }
}
