package com.cqupt.garage.service.impl;

import com.cqupt.garage.dto.PlateModelResultDTO;
import com.cqupt.garage.service.PlateModelClient;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PlateModelClientImpl implements PlateModelClient {

    private static final Pattern CN_PLATE_PATTERN = Pattern.compile("([\\u4e00-\\u9fa5][A-Z][A-Z0-9]{5,6})");
    private static final Pattern COMMON_PLATE_PATTERN = Pattern.compile("([A-Z]{1,2}[A-Z0-9]{5,6})");

    @Override
    public PlateModelResultDTO recognize(byte[] imageBytes, String originalFilename) {
        return recognizeFromFilename(originalFilename);
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
