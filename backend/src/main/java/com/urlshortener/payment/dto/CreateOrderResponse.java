package com.urlshortener.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderResponse {

    private String orderId;
    private Integer amount;   // informational, in paise
    private String currency;
    private String razorpayKeyId; // public key only — NEVER the secret
}
