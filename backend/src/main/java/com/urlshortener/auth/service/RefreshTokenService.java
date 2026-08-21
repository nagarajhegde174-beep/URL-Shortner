package com.urlshortener.auth.service;

import com.urlshortener.auth.model.RefreshToken;
import com.urlshortener.auth.repository.RefreshTokenRepository;
import com.urlshortener.config.AppProperties;
import com.urlshortener.common.exception.UnauthorizedException;
import com.urlshortener.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AppProperties appProperties;

    @Transactional
    public String createRefreshToken(User user) {
        // Remove existing tokens for the user to enforce single session (optional but cleaner)
        refreshTokenRepository.deleteByUser(user);

        String rawToken = UUID.randomUUID().toString();
        String hashedToken = hashToken(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(hashedToken)
                .expiresAt(Instant.now().plusMillis(appProperties.getJwt().getRefreshTokenExpiryMs()))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    @Transactional(readOnly = true)
    public RefreshToken findByToken(String rawToken) {
        String hashedToken = hashToken(rawToken);
        return refreshTokenRepository.findByTokenHash(hashedToken)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
    }

    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new UnauthorizedException("Refresh token was expired. Please sign in again");
        }
        if (token.isRevoked()) {
            throw new UnauthorizedException("Refresh token was revoked");
        }
        return token;
    }

    @Transactional
    public void deleteByToken(String rawToken) {
        String hashedToken = hashToken(rawToken);
        refreshTokenRepository.deleteByTokenHash(hashedToken);
    }

    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
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
