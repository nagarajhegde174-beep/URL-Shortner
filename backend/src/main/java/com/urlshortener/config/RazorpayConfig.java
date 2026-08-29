package com.urlshortener.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RazorpayConfig {

    @Value("${app.razorpay.key-id}")
    private String keyId;

    @Value("${app.razorpay.key-secret}")
    private String keySecret;

    @Bean
    public RazorpayClient razorpayClient() throws RazorpayException {
        log.info("Initializing RazorpayClient with key ID: {}", keyId);
        // keySecret is used here only to construct the client — never logged or returned to clients
        return new RazorpayClient(keyId, keySecret);
    }

    /**
     * Exposes the public key ID for use in responses (e.g. CreateOrderResponse).
     * The key-secret is NEVER exposed outside this config.
     */
    public String getKeyId() {
        return keyId;
    }
}
