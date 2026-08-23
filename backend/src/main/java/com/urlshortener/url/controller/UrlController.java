package com.urlshortener.url.controller;

import com.urlshortener.auth.dto.MessageResponse;
import com.urlshortener.security.CustomUserDetails;
import com.urlshortener.url.dto.CreateUrlRequest;
import com.urlshortener.url.dto.UpdateUrlRequest;
import com.urlshortener.url.dto.UrlResponse;
import com.urlshortener.url.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/links")
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @PostMapping
    public ResponseEntity<UrlResponse> createUrl(
            @Valid @RequestBody CreateUrlRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UrlResponse response = urlService.createUrl(request, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UrlResponse> getUrl(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UrlResponse response = urlService.getUrl(id, userDetails);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<UrlResponse>> listUrls(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<UrlResponse> response = urlService.listUrls(userDetails, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UrlResponse> updateUrl(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUrlRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UrlResponse response = urlService.updateUrl(id, request, userDetails);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteUrl(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        urlService.deleteUrl(id, userDetails);
        return ResponseEntity.ok(new MessageResponse("Link deleted successfully"));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<UrlResponse> toggleUrl(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UrlResponse response = urlService.toggleUrl(id, userDetails);
        return ResponseEntity.ok(response);
    }
}
