package com.cqupt.garage.dto;

import lombok.Data;

@Data
public class MarkPaymentPaidDTO {
    private String orderNo;
    private String transactionNo;
    private String paidTime;
}
