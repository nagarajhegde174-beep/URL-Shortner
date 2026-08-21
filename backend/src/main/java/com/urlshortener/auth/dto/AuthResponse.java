package com.urlshortener.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private UserDetailsDto user;

    @JsonIgnore
    private String refreshToken;

    @Data
    @Builder
    public static class UserDetailsDto {
        private Long id;
        private String email;
        private String role;
        private String plan;
    }
}
