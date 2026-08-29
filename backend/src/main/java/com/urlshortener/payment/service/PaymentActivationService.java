package com.urlshortener.payment.service;

import com.urlshortener.common.exception.ResourceNotFoundException;
import com.urlshortener.payment.entity.Payment;
import com.urlshortener.payment.entity.PaymentStatus;
import com.urlshortener.payment.repository.PaymentRepository;
import com.urlshortener.subscription.model.Subscription;
import com.urlshortener.subscription.model.SubscriptionStatus;
import com.urlshortener.subscription.repository.SubscriptionRepository;
import com.urlshortener.user.model.Plan;
import com.urlshortener.user.model.User;
import com.urlshortener.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Dedicated transactional service for atomic PRO subscription activation.
 *
 * This class is intentionally separate from PaymentService to avoid the
 * Spring @Transactional self-invocation problem. When PaymentService calls
 * this method via the injected bean reference, Spring's proxy correctly
 * intercepts the call and applies the transaction boundary.
 *
 * All three writes (Payment → Subscription → User) are committed atomically.
 * Any failure rolls back all three.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentActivationService {

    private static final int PRO_VALIDITY_DAYS = 30;

    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    /**
     * Activates PRO subscription for the user who owns the given order.
     *
     * This method is idempotent — if the payment is already PAID, it returns
     * immediately without modifying any data.
     *
     * @param orderId   Razorpay order ID (used to locate payment and resolve user)
     * @param paymentId Razorpay payment ID to store
     * @param signature Razorpay signature (may be null for webhook-initiated calls)
     */
    @Transactional
    public void activateSuccessfulPayment(String orderId, String paymentId, String signature) {
        // Step 1: Load Payment with pessimistic write lock to prevent race conditions
        Payment payment = paymentRepository.findByOrderIdWithLock(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment order not found: " + orderId));

        // Step 2: Idempotency guard — never double-activate
        if (payment.getStatus() == PaymentStatus.PAID) {
            log.info("Payment for order {} is already PAID — skipping activation (idempotent)", orderId);
            return;
        }

        // Step 3: Mark payment as PAID
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaymentId(paymentId);
        if (signature != null) {
            payment.setSignature(signature);
        }
        paymentRepository.save(payment);
        log.info("Payment marked PAID for order: {}", orderId);

        // Step 4: Resolve user from Payment record — NOT from webhook payload or frontend
        User user = payment.getUser();
        Instant now = Instant.now();
        Instant expiresAt = now.plus(PRO_VALIDITY_DAYS, ChronoUnit.DAYS);

        // Step 5: Upsert Subscription (create if not exists, update if exists)
        Subscription subscription = subscriptionRepository.findByUserId(user.getId())
                .orElse(Subscription.builder().user(user).build());

        subscription.setPlan(Plan.PRO);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setRazorpayOrderId(orderId);
        subscription.setRazorpayPaymentId(paymentId);
        subscription.setStartedAt(now);
        subscription.setExpiresAt(expiresAt);
        subscriptionRepository.save(subscription);
        log.info("Subscription upserted for user {} — PRO until {}", user.getId(), expiresAt);

        // Step 6: Update user plan to PRO
        user.setPlan(Plan.PRO);
        userRepository.save(user);
        log.info("User {} upgraded to PRO plan", user.getId());
    }
}
