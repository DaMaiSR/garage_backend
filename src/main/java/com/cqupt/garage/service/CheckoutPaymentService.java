package com.cqupt.garage.service;

import com.cqupt.garage.utils.ResultVo;

public interface CheckoutPaymentService {

    ResultVo<Object> createCheckoutOrder(Long recordId, String payMethod, Long requesterUserId, boolean requesterAdmin, String source);

    ResultVo<Object> queryCheckoutOrder(String orderNo, Long requesterUserId, boolean requesterAdmin);

    ResultVo<Object> markCheckoutOrderPaid(String orderNo, String transactionNo, String paidTime, String provider);

    ResultVo<Object> completeCheckout(String orderNo, String outTime, Long requesterUserId, boolean requesterAdmin);
}
