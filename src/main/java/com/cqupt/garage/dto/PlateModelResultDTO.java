package com.cqupt.garage.dto;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class PlateModelResultDTO {

    private boolean success;
    private String plateNo;
    private String provider;
    private Double confidence;
    private String message;

    public static PlateModelResultDTO success(String plateNo, String provider, Double confidence) {
        PlateModelResultDTO dto = new PlateModelResultDTO();
        dto.setSuccess(true);
        dto.setPlateNo(plateNo);
        dto.setProvider(provider);
        dto.setConfidence(confidence);
        return dto;
    }

    public static PlateModelResultDTO fail(String message) {
        PlateModelResultDTO dto = new PlateModelResultDTO();
        dto.setSuccess(false);
        dto.setMessage(message);
        return dto;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("success", success);
        map.put("plateNo", plateNo == null ? "" : plateNo);
        map.put("provider", provider == null ? "" : provider);
        map.put("confidence", confidence == null ? "" : confidence);
        map.put("message", message == null ? "" : message);
        return map;
    }
}
