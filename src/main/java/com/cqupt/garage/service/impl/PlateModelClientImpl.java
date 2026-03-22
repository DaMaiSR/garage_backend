package com.cqupt.garage.service.impl;

import com.cqupt.garage.dto.PlateModelResultDTO;
import com.cqupt.garage.service.PlateModelClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PlateModelClientImpl implements PlateModelClient {

    private static final Pattern CN_PLATE_PATTERN = Pattern.compile("([\\u4e00-\\u9fa5][A-Z][A-Z0-9]{5,6})");
    private static final Pattern COMMON_PLATE_PATTERN = Pattern.compile("([A-Z]{1,2}[A-Z0-9]{5,6})");

    @Value("${integration.plate.model.enabled:false}")
    private boolean modelEnabled;

    @Value("${integration.plate.model.url:http://localhost:9101/api/plate/recognize}")
    private String modelUrl;

    @Value("${integration.plate.model.connect-timeout-ms:3000}")
    private int connectTimeoutMs;

    @Value("${integration.plate.model.read-timeout-ms:6000}")
    private int readTimeoutMs;

    @Value("${integration.plate.model.filename-fallback:true}")
    private boolean filenameFallback;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public PlateModelResultDTO recognize(byte[] imageBytes, String originalFilename) {
        PlateModelResultDTO remoteResult = tryRemoteModel(imageBytes, originalFilename);
        if (remoteResult.isSuccess()) {
            return remoteResult;
        }
        if (!filenameFallback) {
            return remoteResult;
        }
        PlateModelResultDTO fallbackResult = recognizeFromFilename(originalFilename);
        if (fallbackResult.isSuccess()) {
            return fallbackResult;
        }
        String remoteMessage = remoteResult.getMessage() == null ? "" : remoteResult.getMessage().trim();
        String fallbackMessage = fallbackResult.getMessage() == null ? "" : fallbackResult.getMessage().trim();
        String merged = remoteMessage.isEmpty() ? fallbackMessage : remoteMessage + "; " + fallbackMessage;
        return PlateModelResultDTO.fail(merged.isEmpty() ? "未识别到有效车牌" : merged);
    }

    private PlateModelResultDTO tryRemoteModel(byte[] imageBytes, String originalFilename) {
        if (!modelEnabled) {
            return PlateModelResultDTO.fail("模型服务未启用，已切换兜底识别");
        }
        if (imageBytes == null || imageBytes.length == 0) {
            return PlateModelResultDTO.fail("图片内容为空");
        }

        try {
            RestTemplate restTemplate = buildRestTemplate();
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("imageBase64", Base64.getEncoder().encodeToString(imageBytes));
            requestBody.put("fileName", safeFileName(originalFilename));
            requestBody.put("pipeline", "YOLO+LPRNet");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(modelUrl, requestEntity, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                return PlateModelResultDTO.fail("模型服务响应异常，状态码:" + response.getStatusCodeValue());
            }
            return parseModelResponse(response.getBody());
        } catch (Exception ex) {
            return PlateModelResultDTO.fail("调用模型服务失败:" + safeExceptionMessage(ex));
        }
    }

    private PlateModelResultDTO parseModelResponse(String responseBody) {
        if (isBlank(responseBody)) {
            return PlateModelResultDTO.fail("模型服务返回为空");
        }
        try {
            Object root = objectMapper.readValue(responseBody, Object.class);
            String plateNo = normalizePlateNo(asText(findValue(root,
                    "plateNo", "plate_no", "plate", "licensePlate", "license_plate", "plate_number", "number")));
            if (isBlank(plateNo)) {
                return PlateModelResultDTO.fail("模型服务未返回车牌号");
            }

            Double confidence = asDouble(findValue(root,
                    "confidence", "score", "probability", "plateConfidence", "plate_confidence"));
            String provider = asText(findValue(root, "provider", "model", "engine"));
            if (isBlank(provider)) {
                provider = "yolo-lprnet";
            }
            return PlateModelResultDTO.success(plateNo, provider, confidence);
        } catch (Exception ex) {
            return PlateModelResultDTO.fail("解析模型响应失败:" + safeExceptionMessage(ex));
        }
    }

    private PlateModelResultDTO recognizeFromFilename(String originalFilename) {
        if (isBlank(originalFilename)) {
            return PlateModelResultDTO.fail("文件名兜底识别失败");
        }
        String namePart = originalFilename;
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex > 0) {
            namePart = originalFilename.substring(0, dotIndex);
        }
        String normalized = namePart.trim().toUpperCase().replaceAll("[^\\u4E00-\\u9FA5A-Z0-9]", "");
        if (isBlank(normalized)) {
            return PlateModelResultDTO.fail("文件名兜底识别失败");
        }

        Matcher cnMatcher = CN_PLATE_PATTERN.matcher(normalized);
        if (cnMatcher.find()) {
            return PlateModelResultDTO.success(cnMatcher.group(1), "filename-fallback", 0.85D);
        }
        Matcher commonMatcher = COMMON_PLATE_PATTERN.matcher(normalized);
        if (commonMatcher.find()) {
            return PlateModelResultDTO.success(commonMatcher.group(1), "filename-fallback", 0.80D);
        }
        if (normalized.length() >= 6 && normalized.length() <= 10) {
            return PlateModelResultDTO.success(normalized, "filename-fallback", 0.75D);
        }
        return PlateModelResultDTO.fail("文件名兜底识别失败");
    }

    private RestTemplate buildRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Math.max(connectTimeoutMs, 500));
        factory.setReadTimeout(Math.max(readTimeoutMs, 1000));
        return new RestTemplate(factory);
    }

    private Object findValue(Object source, String... keys) {
        if (source == null || keys == null || keys.length == 0) {
            return null;
        }
        if (source instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) source;
            for (String key : keys) {
                Object value = findInMapIgnoreCase(map, key);
                if (!isBlank(asText(value))) {
                    return value;
                }
            }
            for (Object value : map.values()) {
                Object nested = findValue(value, keys);
                if (nested != null) {
                    return nested;
                }
            }
            return null;
        }
        if (source instanceof List) {
            for (Object item : (List<?>) source) {
                Object nested = findValue(item, keys);
                if (nested != null) {
                    return nested;
                }
            }
            return null;
        }
        return null;
    }

    private Object findInMapIgnoreCase(Map<?, ?> map, String key) {
        if (map == null || key == null) {
            return null;
        }
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() != null && key.equalsIgnoreCase(String.valueOf(entry.getKey()))) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String safeFileName(String originalFilename) {
        if (isBlank(originalFilename)) {
            return "upload.jpg";
        }
        return originalFilename.trim();
    }

    private String normalizePlateNo(String plateNo) {
        if (isBlank(plateNo)) {
            return "";
        }
        return plateNo.trim().toUpperCase().replaceAll("[\\s\\-·•・\\.。_]", "");
    }

    private String asText(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private Double asDouble(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Double.valueOf(String.valueOf(value));
        } catch (Exception ex) {
            return null;
        }
    }

    private String safeExceptionMessage(Exception ex) {
        String message = ex == null ? "" : ex.getMessage();
        if (isBlank(message)) {
            return "unknown";
        }
        return message;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
