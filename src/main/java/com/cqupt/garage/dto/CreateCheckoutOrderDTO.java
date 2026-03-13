package com.cqupt.garage.dto;

import lombok.Data;

@Data
public class CreateCheckoutOrderDTO {
    private Long recordId;
    private String payMethod;
}
