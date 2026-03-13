package com.cqupt.garage.integration.payment;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class MockPaymentGateway implements PaymentGateway {

    @Override
    public String provider() {
        return "mock";
    }

    @Override
    public Map<String, String> createOrder(String orderNo, String amount, String payMethod, String subject) {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("provider", provider());
        data.put("providerOrderNo", "MOCK-" + orderNo);
        data.put("payUrl", "https://mockpay.local/checkout?orderNo=" + orderNo + "&amount=" + amount + "&method=" + payMethod);
        data.put("subject", subject == null ? "停车缴费" : subject);
        return data;
    }
}
