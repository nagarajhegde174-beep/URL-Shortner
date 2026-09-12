package com.urlshortener.auth.service;

import com.urlshortener.auth.dto.*;
import com.urlshortener.auth.model.PasswordResetToken;
import com.urlshortener.auth.model.RefreshToken;
import com.urlshortener.auth.repository.PasswordResetTokenRepository;
import com.urlshortener.common.exception.BadRequestException;
import com.urlshortener.redis.service.RedisRateLimiterService;
import com.urlshortener.security.CustomUserDetails;
import com.urlshortener.security.JwtUtil;
import com.urlshortener.subscription.model.Subscription;
import com.urlshortener.subscription.repository.SubscriptionRepository;
import com.urlshortener.url.service.UrlService;
import com.urlshortener.user.model.Plan;
import com.urlshortener.user.model.Role;
import com.urlshortener.user.model.User;
import com.urlshortener.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SubscriptionRepository subscriptionRepository;
    private final UrlService urlService;
    private final EmailService emailService;
    private final RedisRateLimiterService redisRateLimiterService;

    /** Generic message returned for both existing and non-existing emails — prevents account enumeration. */
    private static final String FORGOT_PASSWORD_GENERIC_RESPONSE =
            "If an account exists for this email, a password reset code has been sent.";

    /** Resend cooldown: 1 request per 60 seconds per user. */
    private static final int RESEND_COOLDOWN_SECONDS = 60;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .plan(Plan.FREE)
                .build();

        User savedUser = userRepository.save(user);

        // Generate tokens upon successful registration
        String accessToken = jwtUtil.generateAccessToken(savedUser.getEmail(), savedUser.getRole().name());
        String rawRefreshToken = refreshTokenService.createRefreshToken(savedUser);

        return buildAuthResponse(savedUser, accessToken, rawRefreshToken);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRole().name());
        String rawRefreshToken = refreshTokenService.createRefreshToken(user);

        return buildAuthResponse(user, accessToken, rawRefreshToken);
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        RefreshToken token = refreshTokenService.findByToken(rawRefreshToken);
        RefreshToken verifiedToken = refreshTokenService.verifyExpiration(token);
        User user = verifiedToken.getUser();

        String newAccessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRole().name());
        // Rotate refresh token
        String newRawRefreshToken = refreshTokenService.createRefreshToken(user);

        return buildAuthResponse(user, newAccessToken, newRawRefreshToken);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken != null) {
            refreshTokenService.deleteByToken(rawRefreshToken);
        }
    }

    /**
     * Handles forgot-password requests.
     *
     * Security notes:
     * - Returns the same generic message regardless of whether the email exists (prevents enumeration).
     * - The raw reset token is NEVER returned in the response, never logged, only emailed.
     * - Previous tokens for this user are invalidated before generating a new one.
     * - A per-user resend cooldown is enforced via Redis (1 request per 60 seconds).
     */
    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        // Return generic response for non-existent email — prevents account enumeration
        if (userOpt.isEmpty()) {
            return new MessageResponse(FORGOT_PASSWORD_GENERIC_RESPONSE);
        }

        User user = userOpt.get();

        // Enforce per-user resend cooldown (1 per 60 seconds)
        String cooldownKey = "forgot-pw-cooldown:" + user.getId();
        if (!redisRateLimiterService.isAllowed(cooldownKey, 1, RESEND_COOLDOWN_SECONDS)) {
            throw new BadRequestException(
                    "Please wait before requesting another reset code. A new code can be sent every 60 seconds.");
        }

        // Invalidate any existing reset tokens for this user
        passwordResetTokenRepository.deleteByUser(user);

        // Generate cryptographically secure 6-digit token
        String rawToken = String.format("%06d", new SecureRandom().nextInt(1_000_000));

        // Store only the hash — never the raw token
        String hashedToken = hashToken(rawToken);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .tokenHash(hashedToken)
                .expiresAt(Instant.now().plus(15, ChronoUnit.MINUTES))
                .used(false)
                .build();

        passwordResetTokenRepository.save(resetToken);

        // Send the raw token by email — it is never returned in the response body
        emailService.sendPasswordResetEmail(user.getEmail(), rawToken);

        return new MessageResponse(FORGOT_PASSWORD_GENERIC_RESPONSE);
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        String hashedToken = hashToken(request.getToken().trim());
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(hashedToken)
                .orElseThrow(() -> new BadRequestException("Invalid or expired password reset token"));

        if (token.isUsed()) {
            throw new BadRequestException("Password reset token has already been used");
        }

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Password reset token has expired. Please request a new one");
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        token.setUsed(true);
        passwordResetTokenRepository.save(token);

        // Revoke all existing refresh sessions so old sessions cannot be reused after password change
        refreshTokenService.deleteByUser(user);

        return new MessageResponse("Password has been reset successfully. Please sign in with your new password.");
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUser(CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        boolean isPro = urlService.isUserPro(user);
        Subscription subscription = subscriptionRepository.findByUserId(user.getId()).orElse(null);

        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .plan(isPro ? "PRO" : "FREE")
                .isPro(isPro)
                .subscriptionExpiresAt(subscription != null ? subscription.getExpiresAt() : null)
                .build();
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String rawRefreshToken) {
        AuthResponse.UserDetailsDto userDetails = AuthResponse.UserDetailsDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .plan(user.getPlan().name())
                .build();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .user(userDetails)
                .refreshToken(rawRefreshToken)
                .build();
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}
