package com.urlshortener.payment.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.urlshortener.common.exception.BadRequestException;
import com.urlshortener.common.exception.PaymentVerificationException;
import com.urlshortener.common.exception.ResourceNotFoundException;
import com.urlshortener.common.exception.UnauthorizedException;
import com.urlshortener.config.RazorpayConfig;
import com.urlshortener.payment.dto.CreateOrderRequest;
import com.urlshortener.payment.dto.CreateOrderResponse;
import com.urlshortener.payment.dto.VerifyPaymentRequest;
import com.urlshortener.payment.entity.Payment;
import com.urlshortener.payment.entity.PaymentStatus;
import com.urlshortener.payment.repository.PaymentRepository;
import com.urlshortener.user.model.User;
import com.urlshortener.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    // Server-side pricing — frontend can never override these
    private static final Map<String, Integer> PLAN_PRICES_PAISE = Map.of(
            "PRO", 49900  // ₹499
    );
    private static final String CURRENCY = "INR";
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final RazorpayClient razorpayClient;
    private final RazorpayConfig razorpayConfig;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final PaymentActivationService paymentActivationService;

    @Value("${app.razorpay.key-secret}")
    private String keySecret;

    @Value("${app.razorpay.webhook-secret}")
    private String webhookSecret;

    // ──────────────────────────────────────────────────────────────────────────
    // Create Order
    // ──────────────────────────────────────────────────────────────────────────

    public CreateOrderResponse createOrder(Long userId, CreateOrderRequest request) {
        String planName = request.getPlan().toUpperCase();

        // Server-side amount lookup — frontend never controls the amount
        Integer amountPaise = PLAN_PRICES_PAISE.get(planName);
        if (amountPaise == null) {
            throw new BadRequestException("Unsupported plan: " + planName);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountPaise);
            orderRequest.put("currency", CURRENCY);
            orderRequest.put("receipt", "rcpt_" + userId + "_" + System.currentTimeMillis());

            Order razorpayOrder = razorpayClient.orders.create(orderRequest);
            String orderId = razorpayOrder.get("id");

            // Persist payment record with CREATED status
            Payment payment = Payment.builder()
                    .user(user)
                    .orderId(orderId)
                    .amount(amountPaise)
                    .currency(CURRENCY)
                    .status(PaymentStatus.CREATED)
                    .build();
            paymentRepository.save(payment);

            log.info("Razorpay order created: {} for user: {} plan: {}", orderId, userId, planName);

            return CreateOrderResponse.builder()
                    .orderId(orderId)
                    .amount(amountPaise)
                    .currency(CURRENCY)
                    .razorpayKeyId(razorpayConfig.getKeyId())  // public key only
                    .build();

        } catch (RazorpayException e) {
            log.error("Failed to create Razorpay order for user {}: {}", userId, e.getMessage());
            throw new BadRequestException("Failed to create payment order. Please try again.");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Verify Payment (called by frontend after Razorpay checkout)
    // ──────────────────────────────────────────────────────────────────────────

    public void verifyAndActivate(Long authenticatedUserId, VerifyPaymentRequest request) {
        // Step 1: Load payment to verify ownership
        Payment payment = paymentRepository.findByOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment order not found"));

        // Step 2: Payment ownership check — prevent cross-user activation
        if (!payment.getUser().getId().equals(authenticatedUserId)) {
            log.warn("Payment ownership mismatch: order {} belongs to user {} but requested by user {}",
                    request.getRazorpayOrderId(), payment.getUser().getId(), authenticatedUserId);
            throw new UnauthorizedException("Payment does not belong to this user");
        }

        // Step 3: HMAC-SHA256 signature verification using RAZORPAY_KEY_SECRET
        String payload = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();
        if (!verifyHmac(payload, request.getRazorpaySignature(), keySecret)) {
            log.warn("Payment signature verification failed for order: {}", request.getRazorpayOrderId());
            throw new PaymentVerificationException("Payment signature verification failed");
        }

        log.info("Payment signature verified for order: {}", request.getRazorpayOrderId());

        // Step 4: Delegate to dedicated transactional service (avoids self-invocation proxy bypass)
        paymentActivationService.activateSuccessfulPayment(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Webhook (called by Razorpay)
    // ──────────────────────────────────────────────────────────────────────────

    public void handleWebhook(String rawPayload, String razorpaySignatureHeader) {
        // Step 1: Validate webhook signature using RAZORPAY_WEBHOOK_SECRET (not key-secret)
        if (!verifyHmac(rawPayload, razorpaySignatureHeader, webhookSecret)) {
            log.warn("Invalid Razorpay webhook signature received");
            throw new PaymentVerificationException("Invalid webhook signature");
        }

        // Step 2: Parse event
        JSONObject event = new JSONObject(rawPayload);
        String eventType = event.optString("event", "");
        log.info("Razorpay webhook received: event={}", eventType);

        switch (eventType) {
            case "payment.captured" -> handlePaymentCaptured(event);
            case "payment.failed" -> handlePaymentFailed(event);
            default -> log.info("Unhandled Razorpay webhook event type: {} — ignoring", eventType);
        }
    }

    private void handlePaymentCaptured(JSONObject event) {
        try {
            JSONObject paymentEntity = event
                    .getJSONObject("payload")
                    .getJSONObject("payment")
                    .getJSONObject("entity");

            String orderId = paymentEntity.getString("order_id");
            String paymentId = paymentEntity.getString("id");

            // Resolve user via orderId → Payment → User (never trust webhook payload for userId)
            paymentRepository.findByOrderId(orderId).ifPresentOrElse(
                    p -> {
                        log.info("Webhook: activating payment for order {} (user {})", orderId, p.getUser().getId());
                        // Delegate to dedicated transactional service
                        paymentActivationService.activateSuccessfulPayment(orderId, paymentId, null);
                    },
                    () -> log.warn("Webhook payment.captured: order {} not found in DB — ignoring", orderId)
            );
        } catch (Exception e) {
            log.error("Failed to process payment.captured webhook: {}", e.getMessage());
            throw new BadRequestException("Malformed webhook payload");
        }
    }

    private void handlePaymentFailed(JSONObject event) {
        try {
            JSONObject paymentEntity = event
                    .getJSONObject("payload")
                    .getJSONObject("payment")
                    .getJSONObject("entity");

            String orderId = paymentEntity.getString("order_id");

            paymentRepository.findByOrderId(orderId).ifPresent(payment -> {
                if (payment.getStatus() != PaymentStatus.PAID) {
                    payment.setStatus(PaymentStatus.FAILED);
                    paymentRepository.save(payment);
                    log.info("Payment marked FAILED for order: {}", orderId);
                }
            });
        } catch (Exception e) {
            log.error("Failed to process payment.failed webhook: {}", e.getMessage());
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // HMAC-SHA256 Helper — never logs the secret
    // ──────────────────────────────────────────────────────────────────────────

    private boolean verifyHmac(String payload, String expectedSignature, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(keySpec);
            byte[] computedHash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String computedHex = bytesToHex(computedHash);
            // Constant-time comparison to prevent timing attacks
            return MessageDigest.isEqual(
                    computedHex.getBytes(StandardCharsets.UTF_8),
                    expectedSignature.getBytes(StandardCharsets.UTF_8)
            );
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("HMAC verification error: {}", e.getMessage());
            return false;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}
