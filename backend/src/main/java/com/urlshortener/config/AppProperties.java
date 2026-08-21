package com.urlshortener.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {

    private String baseUrl;
    private final Cookie cookie = new Cookie();
    private final Jwt jwt = new Jwt();
    private final Razorpay razorpay = new Razorpay();

    @Getter
    @Setter
    public static class Cookie {
        private boolean secure;
        private String sameSite = "Strict";
    }

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private long accessTokenExpiryMs;
        private long refreshTokenExpiryMs;
    }

    @Getter
    @Setter
    public static class Razorpay {
        private String keyId;
        private String keySecret;
        private String webhookSecret;
    }
}
