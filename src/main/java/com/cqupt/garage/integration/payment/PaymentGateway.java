package com.cqupt.garage.integration.payment;

import java.util.Map;

public interface PaymentGateway {

    String provider();

    Map<String, String> createOrder(String orderNo, String amount, String payMethod, String subject);
}
