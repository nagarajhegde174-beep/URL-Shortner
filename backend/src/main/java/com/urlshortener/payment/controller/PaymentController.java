package com.urlshortener.payment.controller;

import com.urlshortener.auth.dto.MessageResponse;
import com.urlshortener.payment.dto.CreateOrderRequest;
import com.urlshortener.payment.dto.CreateOrderResponse;
import com.urlshortener.payment.dto.VerifyPaymentRequest;
import com.urlshortener.payment.service.PaymentService;
import com.urlshortener.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Create a Razorpay order (authenticated).
     * Returns orderId + public razorpayKeyId. Never returns key-secret.
     */
    @PostMapping("/orders")
    public ResponseEntity<CreateOrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        CreateOrderResponse response = paymentService.createOrder(userDetails.getId(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * Verify payment signature and activate PRO (authenticated).
     * Frontend calls this after Razorpay checkout popup success.
     */
    @PostMapping("/verify")
    public ResponseEntity<MessageResponse> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        paymentService.verifyAndActivate(userDetails.getId(), request);
        return ResponseEntity.ok(new MessageResponse("Payment verified. PRO plan activated successfully."));
    }

    /**
     * Razorpay webhook endpoint (PUBLIC — no JWT required).
     * Consumes raw payload body and X-Razorpay-Signature header.
     * Returns 200 on valid processing, 400 on invalid signature.
     */
    @PostMapping(value = "/webhook", consumes = "application/json")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String rawPayload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String razorpaySignature) {

        if (razorpaySignature == null || razorpaySignature.isBlank()) {
            log.warn("Webhook received without X-Razorpay-Signature header — rejecting");
            return ResponseEntity.badRequest().build();
        }

        paymentService.handleWebhook(rawPayload, razorpaySignature);
        return ResponseEntity.ok().build();
    }
}
