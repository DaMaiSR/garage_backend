package com.cqupt.garage.integration.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentGatewayRouter {

    private final List<PaymentGateway> gateways;

    @Value("${integration.payment.provider:mock}")
    private String provider;

    public PaymentGatewayRouter(List<PaymentGateway> gateways) {
        this.gateways = gateways;
    }

    public PaymentGateway route() {
        if (gateways == null || gateways.isEmpty()) {
            throw new IllegalStateException("payment gateway is not configured");
        }
        String configured = provider == null ? "" : provider.trim().toLowerCase();
        for (PaymentGateway gateway : gateways) {
            if (gateway != null && gateway.provider().equalsIgnoreCase(configured)) {
                return gateway;
            }
        }
        return gateways.get(0);
    }
}
