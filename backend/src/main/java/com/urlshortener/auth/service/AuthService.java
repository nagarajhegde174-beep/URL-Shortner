package com.urlshortener.auth.service;

import com.urlshortener.auth.dto.AuthResponse;
import com.urlshortener.auth.dto.LoginRequest;
import com.urlshortener.auth.dto.RegisterRequest;
import com.urlshortener.auth.dto.UserProfileResponse;
import com.urlshortener.auth.model.RefreshToken;
import com.urlshortener.config.AppProperties;
import com.urlshortener.common.exception.BadRequestException;
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

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
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
}
