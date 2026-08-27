package com.urlshortener.analytics.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserAgentParserTest {

    @Test
    public void testParseBrowsers() {
        // Chrome
        String chromeUa = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
        assertEquals("Chrome", UserAgentParser.parseBrowser(chromeUa));

        // Mobile Chrome
        String mobileChromeUa = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36";
        assertEquals("Mobile Chrome", UserAgentParser.parseBrowser(mobileChromeUa));

        // Firefox
        String firefoxUa = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/121.0";
        assertEquals("Firefox", UserAgentParser.parseBrowser(firefoxUa));

        // Safari
        String safariUa = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Safari/605.1.15";
        assertEquals("Safari", UserAgentParser.parseBrowser(safariUa));

        // Edge
        String edgeUa = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0";
        assertEquals("Edge", UserAgentParser.parseBrowser(edgeUa));

        // Unknown
        assertEquals("Unknown", UserAgentParser.parseBrowser(null));
        assertEquals("Unknown", UserAgentParser.parseBrowser(""));
        assertEquals("Unknown", UserAgentParser.parseBrowser("MyCustomHTTPClient/1.0"));
    }

    @Test
    public void testParseOS() {
        // Windows
        String winUa = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
        assertEquals("Windows", UserAgentParser.parseOS(winUa));

        // macOS
        String macUa = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)";
        assertEquals("macOS", UserAgentParser.parseOS(macUa));

        // Linux
        String linuxUa = "Mozilla/5.0 (X11; Linux x86_64)";
        assertEquals("Linux", UserAgentParser.parseOS(linuxUa));

        // Android
        String androidUa = "Mozilla/5.0 (Linux; Android 13; Pixel 6)";
        assertEquals("Android", UserAgentParser.parseOS(androidUa));

        // iOS
        String iosUa = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_2 like Mac OS X)";
        assertEquals("iOS", UserAgentParser.parseOS(iosUa));

        // Unknown
        assertEquals("Unknown", UserAgentParser.parseOS(null));
        assertEquals("Unknown", UserAgentParser.parseOS(""));
    }

    @Test
    public void testParseDevice() {
        // Desktop (default)
        String desktopUa = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
        assertEquals("Desktop", UserAgentParser.parseDevice(desktopUa));

        // Mobile
        String mobileUa = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_2 like Mac OS X) AppleWebKit/605.1.15 Mobile/15E148";
        assertEquals("Mobile", UserAgentParser.parseDevice(mobileUa));

        // Tablet
        String tabletUa = "Mozilla/5.0 (iPad; CPU OS 17_2 like Mac OS X) AppleWebKit/605.1.15";
        assertEquals("Tablet", UserAgentParser.parseDevice(tabletUa));

        // Unknown
        assertEquals("Desktop", UserAgentParser.parseDevice(null));
        assertEquals("Desktop", UserAgentParser.parseDevice(""));
    }
}
