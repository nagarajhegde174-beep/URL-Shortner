package com.urlshortener.auth.controller;

import com.urlshortener.auth.dto.AuthResponse;
import com.urlshortener.auth.dto.LoginRequest;
import com.urlshortener.auth.dto.MessageResponse;
import com.urlshortener.auth.dto.RegisterRequest;
import com.urlshortener.auth.service.AuthService;
import com.urlshortener.config.AppProperties;
import com.urlshortener.common.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AppProperties appProperties;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        AuthResponse authResponse = authService.register(request);
        setRefreshTokenCookie(response, authResponse.getRefreshToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request);
        setRefreshTokenCookie(response, authResponse.getRefreshToken());
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = "refresh_token", required = false) String rawRefreshToken,
            HttpServletResponse response) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new UnauthorizedException("Refresh token is missing");
        }
        AuthResponse authResponse = authService.refresh(rawRefreshToken);
        setRefreshTokenCookie(response, authResponse.getRefreshToken());
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(
            @CookieValue(name = "refresh_token", required = false) String rawRefreshToken,
            HttpServletResponse response) {
        authService.logout(rawRefreshToken);
        clearRefreshTokenCookie(response);
        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String rawRefreshToken) {
        if (rawRefreshToken == null) return;

        ResponseCookie cookie = ResponseCookie.from("refresh_token", rawRefreshToken)
                .httpOnly(true)
                .secure(appProperties.getCookie().isSecure())
                .path("/")
                .maxAge(appProperties.getJwt().getRefreshTokenExpiryMs() / 1000)
                .sameSite(appProperties.getCookie().getSameSite())
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(appProperties.getCookie().isSecure())
                .path("/")
                .maxAge(0)
                .sameSite(appProperties.getCookie().getSameSite())
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
