package com.urlshortener.auth.service;

import com.urlshortener.auth.dto.*;
import com.urlshortener.auth.model.PasswordResetToken;
import com.urlshortener.auth.model.RefreshToken;
import com.urlshortener.auth.repository.PasswordResetTokenRepository;
import com.urlshortener.config.AppProperties;
import com.urlshortener.common.exception.BadRequestException;
import com.urlshortener.common.exception.ResourceNotFoundException;
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

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SubscriptionRepository subscriptionRepository;
    private final UrlService urlService;

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

    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with this email address"));

        // Delete previous reset tokens for this user
        passwordResetTokenRepository.deleteByUser(user);

        // Generate secure 6-digit token code
        String rawToken = String.format("%06d", new SecureRandom().nextInt(1000000));
        String hashedToken = hashToken(rawToken);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .tokenHash(hashedToken)
                .expiresAt(Instant.now().plus(15, ChronoUnit.MINUTES))
                .used(false)
                .build();

        passwordResetTokenRepository.save(resetToken);

        return ForgotPasswordResponse.builder()
                .message("Password reset token generated successfully.")
                .resetToken(rawToken)
                .expiresInMinutes(15)
                .build();
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

        // Revoke existing session tokens
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

