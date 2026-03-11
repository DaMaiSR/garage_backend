package com.cqupt.garage.dto;

import lombok.Data;

@Data
public class GarageReservationDTO {
    private String plateNo;
    private String spaceNo;
    private String reservationStatus;
    private Integer pageSize;
}
