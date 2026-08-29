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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentActivationServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PaymentActivationService paymentActivationService;

    private User testUser;
    private Payment testPayment;
    private Subscription testSubscription;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .plan(Plan.FREE)
                .build();

        testPayment = Payment.builder()
                .id(10L)
                .user(testUser)
                .orderId("order_123")
                .amount(49900)
                .currency("INR")
                .status(PaymentStatus.CREATED)
                .build();

        testSubscription = Subscription.builder()
                .id(100L)
                .user(testUser)
                .plan(Plan.FREE)
                .status(SubscriptionStatus.ACTIVE)
                .build();
    }

    @Test
    void activateSuccessfulPayment_NewSubscription_Success() {
        when(paymentRepository.findByOrderIdWithLock("order_123")).thenReturn(Optional.of(testPayment));
        when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        paymentActivationService.activateSuccessfulPayment("order_123", "pay_123", "sig_123");

        assertEquals(PaymentStatus.PAID, testPayment.getStatus());
        assertEquals("pay_123", testPayment.getPaymentId());
        assertEquals("sig_123", testPayment.getSignature());

        verify(paymentRepository).save(testPayment);
        verify(subscriptionRepository).save(argThat(sub -> {
            assertEquals(Plan.PRO, sub.getPlan());
            assertEquals(SubscriptionStatus.ACTIVE, sub.getStatus());
            assertEquals("order_123", sub.getRazorpayOrderId());
            assertEquals("pay_123", sub.getRazorpayPaymentId());
            assertNotNull(sub.getStartedAt());
            assertNotNull(sub.getExpiresAt());
            return true;
        }));
        verify(userRepository).save(argThat(u -> {
            assertEquals(Plan.PRO, u.getPlan());
            return true;
        }));
    }

    @Test
    void activateSuccessfulPayment_ExistingSubscription_Success() {
        when(paymentRepository.findByOrderIdWithLock("order_123")).thenReturn(Optional.of(testPayment));
        when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.of(testSubscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        paymentActivationService.activateSuccessfulPayment("order_123", "pay_123", "sig_123");

        assertEquals(PaymentStatus.PAID, testPayment.getStatus());
        verify(paymentRepository).save(testPayment);
        verify(subscriptionRepository).save(testSubscription);
        assertEquals(Plan.PRO, testSubscription.getPlan());
        assertEquals(SubscriptionStatus.ACTIVE, testSubscription.getStatus());
        verify(userRepository).save(testUser);
        assertEquals(Plan.PRO, testUser.getPlan());
    }

    @Test
    void activateSuccessfulPayment_AlreadyPaid_IdempotentNoOp() {
        testPayment.setStatus(PaymentStatus.PAID);
        when(paymentRepository.findByOrderIdWithLock("order_123")).thenReturn(Optional.of(testPayment));

        paymentActivationService.activateSuccessfulPayment("order_123", "pay_123", "sig_123");

        verify(paymentRepository, never()).save(any(Payment.class));
        verify(subscriptionRepository, never()).save(any(Subscription.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void activateSuccessfulPayment_PaymentNotFound_ThrowsException() {
        when(paymentRepository.findByOrderIdWithLock("order_123")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                paymentActivationService.activateSuccessfulPayment("order_123", "pay_123", "sig_123")
        );

        verify(paymentRepository, never()).save(any(Payment.class));
        verify(subscriptionRepository, never()).save(any(Subscription.class));
        verify(userRepository, never()).save(any(User.class));
    }
}
