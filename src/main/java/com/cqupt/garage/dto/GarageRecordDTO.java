package com.cqupt.garage.dto;

import lombok.Data;

@Data
public class GarageRecordDTO {
    private String plateNo;
    private String recordStatus;
    private Integer pageSize;
}
