package com.urlshortener.analytics.util;

public class UserAgentParser {

    public static String parseBrowser(String ua) {
        if (ua == null || ua.isEmpty()) return "Unknown";
        String lowerUa = ua.toLowerCase();

        if (lowerUa.contains("edg/")) {
            return "Edge";
        }
        if (lowerUa.contains("firefox") || lowerUa.contains("fxios")) {
            return "Firefox";
        }
        if (lowerUa.contains("chrome") || lowerUa.contains("crios")) {
            if (lowerUa.contains("mobile")) {
                return "Mobile Chrome";
            }
            return "Chrome";
        }
        if (lowerUa.contains("safari") && !lowerUa.contains("chrome") && !lowerUa.contains("android")) {
            return "Safari";
        }
        return "Unknown";
    }

    public static String parseOS(String ua) {
        if (ua == null || ua.isEmpty()) return "Unknown";
        String lowerUa = ua.toLowerCase();

        if (lowerUa.contains("android")) {
            return "Android";
        }
        if (lowerUa.contains("iphone") || lowerUa.contains("ipad") || lowerUa.contains("ipod")) {
            return "iOS";
        }
        if (lowerUa.contains("windows")) {
            return "Windows";
        }
        if (lowerUa.contains("macintosh") || lowerUa.contains("mac os x")) {
            return "macOS";
        }
        if (lowerUa.contains("linux")) {
            return "Linux";
        }
        return "Unknown";
    }

    public static String parseDevice(String ua) {
        if (ua == null || ua.isEmpty()) return "Desktop";
        String lowerUa = ua.toLowerCase();

        if (lowerUa.contains("ipad")) {
            return "Tablet";
        }
        if (lowerUa.contains("mobile") || lowerUa.contains("iphone") || lowerUa.contains("android")) {
            if (lowerUa.contains("tablet")) {
                return "Tablet";
            }
            return "Mobile";
        }
        return "Desktop";
    }
}
