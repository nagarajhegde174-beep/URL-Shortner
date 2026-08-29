package com.urlshortener.payment.service;

import com.razorpay.Order;
import com.razorpay.OrderClient;
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
import com.urlshortener.user.model.Plan;
import com.urlshortener.user.model.User;
import com.urlshortener.user.repository.UserRepository;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private RazorpayClient razorpayClient;

    @Mock
    private RazorpayConfig razorpayConfig;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentActivationService paymentActivationService;

    @InjectMocks
    private PaymentService paymentService;

    // Public mock field for nested orders
    private OrderClient orderClient;

    private User testUser;
    private Payment testPayment;

    @BeforeEach
    void setUp() {
        orderClient = mock(OrderClient.class);
        // Inject mock orderClient into public orders field of razorpayClient
        razorpayClient.orders = orderClient;

        // Set value properties via ReflectionTestUtils
        ReflectionTestUtils.setField(paymentService, "keySecret", "rzp_secret_123");
        ReflectionTestUtils.setField(paymentService, "webhookSecret", "wh_secret_123");

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
    }

    @Test
    void createOrder_ProPlan_Success() throws RazorpayException {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setPlan("PRO");

        Order mockOrder = mock(Order.class);
        when(mockOrder.get("id")).thenReturn("order_123");
        when(orderClient.create(any(JSONObject.class))).thenReturn(mockOrder);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(razorpayConfig.getKeyId()).thenReturn("rzp_key_id_123");

        CreateOrderResponse res = paymentService.createOrder(1L, req);

        assertNotNull(res);
        assertEquals("order_123", res.getOrderId());
        assertEquals(49900, res.getAmount());
        assertEquals("INR", res.getCurrency());
        assertEquals("rzp_key_id_123", res.getRazorpayKeyId());

        verify(paymentRepository).save(argThat(payment -> {
            assertEquals("order_123", payment.getOrderId());
            assertEquals(49900, payment.getAmount());
            assertEquals(PaymentStatus.CREATED, payment.getStatus());
            assertEquals(testUser, payment.getUser());
            return true;
        }));
    }

    @Test
    void createOrder_UnknownPlan_ThrowsBadRequest() {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setPlan("UNKNOWN_PLAN");

        assertThrows(BadRequestException.class, () -> paymentService.createOrder(1L, req));
        verifyNoInteractions(orderClient, paymentRepository);
    }

    @Test
    void verifyAndActivate_ValidSignature_Success() {
        VerifyPaymentRequest req = new VerifyPaymentRequest();
        req.setRazorpayOrderId("order_123");
        req.setRazorpayPaymentId("pay_123");
        String sig = computeHmacSha256("order_123|pay_123", "rzp_secret_123");
        req.setRazorpaySignature(sig);

        when(paymentRepository.findByOrderId("order_123")).thenReturn(Optional.of(testPayment));

        paymentService.verifyAndActivate(1L, req);

        verify(paymentActivationService).activateSuccessfulPayment(
                "order_123", "pay_123", sig
        );
    }

    @Test
    void verifyAndActivate_WrongOwner_ThrowsUnauthorized() {
        VerifyPaymentRequest req = new VerifyPaymentRequest();
        req.setRazorpayOrderId("order_123");
        req.setRazorpayPaymentId("pay_123");
        String sig = computeHmacSha256("order_123|pay_123", "rzp_secret_123");
        req.setRazorpaySignature(sig);

        // Authenticated user is 2L, but payment owner is 1L
        when(paymentRepository.findByOrderId("order_123")).thenReturn(Optional.of(testPayment));

        assertThrows(UnauthorizedException.class, () -> paymentService.verifyAndActivate(2L, req));
        verifyNoInteractions(paymentActivationService);
    }

    @Test
    void verifyAndActivate_InvalidSignature_ThrowsPaymentVerificationException() {
        VerifyPaymentRequest req = new VerifyPaymentRequest();
        req.setRazorpayOrderId("order_123");
        req.setRazorpayPaymentId("pay_123");
        req.setRazorpaySignature("invalid_signature");

        when(paymentRepository.findByOrderId("order_123")).thenReturn(Optional.of(testPayment));

        assertThrows(PaymentVerificationException.class, () -> paymentService.verifyAndActivate(1L, req));
        verifyNoInteractions(paymentActivationService);
    }

    @Test
    void handleWebhook_ValidSignature_Captured_Success() {
        // Prepare mock JSON raw payload
        JSONObject mockPayload = new JSONObject();
        mockPayload.put("event", "payment.captured");
        
        JSONObject paymentEntity = new JSONObject();
        paymentEntity.put("order_id", "order_123");
        paymentEntity.put("id", "pay_123");

        JSONObject paymentWrapper = new JSONObject();
        paymentWrapper.put("entity", paymentEntity);

        JSONObject payloadContent = new JSONObject();
        payloadContent.put("payment", paymentWrapper);

        mockPayload.put("payload", payloadContent);

        String rawPayload = mockPayload.toString();
        // Compute signature for rawPayload with secret "wh_secret_123"
        // Let's compute computed signature manually using helper
        String signature = computeHmacSha256(rawPayload, "wh_secret_123");

        when(paymentRepository.findByOrderId("order_123")).thenReturn(Optional.of(testPayment));

        paymentService.handleWebhook(rawPayload, signature);

        verify(paymentActivationService).activateSuccessfulPayment("order_123", "pay_123", null);
    }

    @Test
    void handleWebhook_InvalidSignature_ThrowsPaymentVerificationException() {
        assertThrows(PaymentVerificationException.class, () -> 
                paymentService.handleWebhook("raw_payload", "invalid_signature")
        );
        verifyNoInteractions(paymentActivationService);
    }

    @Test
    void handleWebhook_Captured_UnknownOrder_LogsWarning() {
        JSONObject mockPayload = new JSONObject();
        mockPayload.put("event", "payment.captured");
        
        JSONObject paymentEntity = new JSONObject();
        paymentEntity.put("order_id", "unknown_order");
        paymentEntity.put("id", "pay_123");

        JSONObject paymentWrapper = new JSONObject();
        paymentWrapper.put("entity", paymentEntity);

        JSONObject payloadContent = new JSONObject();
        payloadContent.put("payment", paymentWrapper);

        mockPayload.put("payload", payloadContent);

        String rawPayload = mockPayload.toString();
        String signature = computeHmacSha256(rawPayload, "wh_secret_123");

        when(paymentRepository.findByOrderId("unknown_order")).thenReturn(Optional.empty());

        paymentService.handleWebhook(rawPayload, signature);

        verifyNoInteractions(paymentActivationService);
    }

    @Test
    void handleWebhook_Failed_Success() {
        JSONObject mockPayload = new JSONObject();
        mockPayload.put("event", "payment.failed");
        
        JSONObject paymentEntity = new JSONObject();
        paymentEntity.put("order_id", "order_123");

        JSONObject paymentWrapper = new JSONObject();
        paymentWrapper.put("entity", paymentEntity);

        JSONObject payloadContent = new JSONObject();
        payloadContent.put("payment", paymentWrapper);

        mockPayload.put("payload", payloadContent);

        String rawPayload = mockPayload.toString();
        String signature = computeHmacSha256(rawPayload, "wh_secret_123");

        when(paymentRepository.findByOrderId("order_123")).thenReturn(Optional.of(testPayment));

        paymentService.handleWebhook(rawPayload, signature);

        assertEquals(PaymentStatus.FAILED, testPayment.getStatus());
        verify(paymentRepository).save(testPayment);
    }

    private String computeHmacSha256(String payload, String secret) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec keySpec = new javax.crypto.spec.SecretKeySpec(
                    secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256"
            );
            mac.init(keySpec);
            byte[] hash = mac.doFinal(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
