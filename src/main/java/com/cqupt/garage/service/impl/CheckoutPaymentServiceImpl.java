package com.cqupt.garage.service.impl;

import com.cqupt.garage.integration.payment.PaymentGateway;
import com.cqupt.garage.integration.payment.PaymentGatewayRouter;
import com.cqupt.garage.pojo.GarageRecord;
import com.cqupt.garage.service.CheckoutPaymentService;
import com.cqupt.garage.service.GarageRecordService;
import com.cqupt.garage.utils.DateTimeUtils;
import com.cqupt.garage.utils.ResultVo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CheckoutPaymentServiceImpl implements CheckoutPaymentService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_PAID = "PAID";
    private static final String STATUS_DONE = "DONE";

    private final GarageRecordService garageRecordService;
    private final PaymentGatewayRouter paymentGatewayRouter;
    private final ConcurrentMap<String, CheckoutPaymentOrder> orderStore = new ConcurrentHashMap<>();

    @Value("${integration.payment.order-expire-minutes:15}")
    private int orderExpireMinutes;

    public CheckoutPaymentServiceImpl(GarageRecordService garageRecordService,
                                      PaymentGatewayRouter paymentGatewayRouter) {
        this.garageRecordService = garageRecordService;
        this.paymentGatewayRouter = paymentGatewayRouter;
    }

    @Override
    public ResultVo<Object> createCheckoutOrder(Long recordId, String payMethod, Long requesterUserId, boolean requesterAdmin, String source) {
        if (recordId == null) {
            return ResultVo.fail("记录ID不能为空");
        }
        GarageRecord record = garageRecordService.getById(recordId);
        if (record == null) {
            return ResultVo.fail("停车记录不存在");
        }
        if (!requesterAdmin && (requesterUserId == null || !requesterUserId.equals(record.getUserId()))) {
            return ResultVo.fail("无权限创建支付订单");
        }
        if (!"0".equals(record.getRecordStatus())) {
            return ResultVo.fail("该记录已出库，不能重复支付");
        }

        String normalizedPayMethod = normalizePayMethod(payMethod);
        if (isBlank(normalizedPayMethod)) {
            return ResultVo.fail("支付方式不支持");
        }
        long estimatedMinutes = DateTimeUtils.diffMinutes(record.getInTime(), DateTimeUtils.nowDateTime());
        String estimatedFee = DateTimeUtils.calcFeeByMinutes(estimatedMinutes);
        if (!"0".equals(estimatedFee) && "FREE".equals(normalizedPayMethod)) {
            return ResultVo.fail("有费用时不能使用免单");
        }

        String orderNo = buildOrderNo();
        PaymentGateway gateway = paymentGatewayRouter.route();
        Map<String, String> gatewayData = gateway.createOrder(
                orderNo,
                estimatedFee,
                normalizedPayMethod,
                "停车缴费-" + record.getPlateNo()
        );

        CheckoutPaymentOrder order = new CheckoutPaymentOrder();
        order.orderNo = orderNo;
        order.recordId = record.getId();
        order.userId = record.getUserId();
        order.plateNo = record.getPlateNo();
        order.amount = estimatedFee;
        order.payMethod = normalizedPayMethod;
        order.source = isBlank(source) ? "MANUAL" : source.trim().toUpperCase();
        order.provider = gatewayData.get("provider");
        order.providerOrderNo = gatewayData.get("providerOrderNo");
        order.payUrl = gatewayData.get("payUrl");
        order.createdTime = DateTimeUtils.nowDateTime();
        order.orderStatus = ("0".equals(estimatedFee) && "FREE".equals(normalizedPayMethod)) ? STATUS_PAID : STATUS_PENDING;
        if (STATUS_PAID.equals(order.orderStatus)) {
            order.paidTime = order.createdTime;
            order.transactionNo = "FREE-" + orderNo;
        }
        orderStore.put(orderNo, order);
        return ResultVo.ok(toView(order), "支付订单创建成功");
    }

    @Override
    public ResultVo<Object> queryCheckoutOrder(String orderNo, Long requesterUserId, boolean requesterAdmin) {
        if (isBlank(orderNo)) {
            return ResultVo.fail("订单号不能为空");
        }
        CheckoutPaymentOrder order = orderStore.get(orderNo.trim());
        if (order == null) {
            return ResultVo.fail("订单不存在");
        }
        if (!requesterAdmin && (requesterUserId == null || !requesterUserId.equals(order.userId))) {
            return ResultVo.fail("无权限查看该订单");
        }
        return ResultVo.ok(toView(order));
    }

    @Override
    public ResultVo<Object> markCheckoutOrderPaid(String orderNo, String transactionNo, String paidTime, String provider) {
        if (isBlank(orderNo)) {
            return ResultVo.fail("订单号不能为空");
        }
        CheckoutPaymentOrder order = orderStore.get(orderNo.trim());
        if (order == null) {
            return ResultVo.fail("订单不存在");
        }
        if (STATUS_DONE.equals(order.orderStatus)) {
            return ResultVo.fail("订单已完成结算");
        }
        order.orderStatus = STATUS_PAID;
        order.transactionNo = isBlank(transactionNo) ? "TXN-" + order.orderNo : transactionNo.trim();
        order.paidTime = DateTimeUtils.isValidDateTime(paidTime) ? paidTime.trim() : DateTimeUtils.nowDateTime();
        if (!isBlank(provider)) {
            order.provider = provider.trim();
        }
        return ResultVo.ok(toView(order), "支付回调已接收");
    }

    @Override
    public ResultVo<Object> completeCheckout(String orderNo, String outTime, Long requesterUserId, boolean requesterAdmin) {
        if (isBlank(orderNo)) {
            return ResultVo.fail("订单号不能为空");
        }
        CheckoutPaymentOrder order = orderStore.get(orderNo.trim());
        if (order == null) {
            return ResultVo.fail("订单不存在");
        }
        if (!requesterAdmin && (requesterUserId == null || !requesterUserId.equals(order.userId))) {
            return ResultVo.fail("无权限完成该订单");
        }
        if (STATUS_DONE.equals(order.orderStatus)) {
            return ResultVo.fail("订单已完成");
        }
        if (!STATUS_PAID.equals(order.orderStatus)) {
            return ResultVo.fail("订单尚未支付");
        }

        GarageRecord outCommand = new GarageRecord();
        outCommand.setId(order.recordId);
        outCommand.setOutTime(isBlank(outTime) ? DateTimeUtils.nowDateTime() : outTime.trim());
        outCommand.setPayMethod(order.payMethod);
        ResultVo<Object> checkoutResult = garageRecordService.updateGarageOutRecord(outCommand);
        if (!checkoutResult.isFlag()) {
            return checkoutResult;
        }

        order.orderStatus = STATUS_DONE;
        order.completedTime = DateTimeUtils.nowDateTime();
        return ResultVo.ok(toView(order), "订单完成并已出库");
    }

    private String buildOrderNo() {
        return "CO" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(100, 999);
    }

    private Map<String, Object> toView(CheckoutPaymentOrder order) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("orderNo", order.orderNo);
        data.put("recordId", order.recordId);
        data.put("userId", order.userId);
        data.put("plateNo", order.plateNo);
        data.put("amount", order.amount);
        data.put("payMethod", order.payMethod);
        data.put("orderStatus", order.orderStatus);
        data.put("source", order.source);
        data.put("provider", order.provider);
        data.put("providerOrderNo", order.providerOrderNo);
        data.put("payUrl", order.payUrl);
        data.put("transactionNo", order.transactionNo);
        data.put("createdTime", order.createdTime);
        data.put("paidTime", order.paidTime);
        data.put("completedTime", order.completedTime);
        data.put("expireMinutes", orderExpireMinutes);
        return data;
    }

    private String normalizePayMethod(String payMethod) {
        if (isBlank(payMethod)) {
            return "WECHAT";
        }
        String method = payMethod.trim().toUpperCase();
        if ("WECHAT".equals(method) || "ALIPAY".equals(method) || "CASH".equals(method) || "FREE".equals(method)) {
            return method;
        }
        if ("微信".equals(payMethod.trim()) || "微信支付".equals(payMethod.trim())) {
            return "WECHAT";
        }
        if ("支付宝".equals(payMethod.trim())) {
            return "ALIPAY";
        }
        if ("现金".equals(payMethod.trim())) {
            return "CASH";
        }
        if ("免单".equals(payMethod.trim())) {
            return "FREE";
        }
        return "";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static class CheckoutPaymentOrder {
        private String orderNo;
        private Long recordId;
        private Long userId;
        private String plateNo;
        private String amount;
        private String payMethod;
        private String orderStatus;
        private String source;
        private String provider;
        private String providerOrderNo;
        private String payUrl;
        private String transactionNo;
        private String createdTime;
        private String paidTime;
        private String completedTime;
    }
}
