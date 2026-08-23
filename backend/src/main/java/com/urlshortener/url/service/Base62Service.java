package com.urlshortener.url.service;

import org.springframework.stereotype.Service;
import java.security.SecureRandom;

@Service
public class Base62Service {
    private static final String BASE62 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final SecureRandom random = new SecureRandom();

    public String generateShortCode() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(BASE62.charAt(random.nextInt(62)));
        }
        return sb.toString();
    }
}
