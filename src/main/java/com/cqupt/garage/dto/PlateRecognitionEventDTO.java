package com.cqupt.garage.dto;

import lombok.Data;

@Data
public class PlateRecognitionEventDTO {
    private String plateNo;
    private String action;
    private String cameraCode;
    private String eventTime;
    private String spaceNo;
}
