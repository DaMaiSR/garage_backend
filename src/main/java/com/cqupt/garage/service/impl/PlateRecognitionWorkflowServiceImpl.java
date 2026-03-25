package com.cqupt.garage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cqupt.garage.dto.PlateModelResultDTO;
import com.cqupt.garage.dto.PlateRecognitionEventDTO;
import com.cqupt.garage.mapper.GarageRecordMapper;
import com.cqupt.garage.mapper.GarageReservationMapper;
import com.cqupt.garage.mapper.GarageSpaceMapper;
import com.cqupt.garage.mapper.GarageVehicleMapper;
import com.cqupt.garage.pojo.GarageRecord;
import com.cqupt.garage.pojo.GarageReservation;
import com.cqupt.garage.pojo.GarageSpace;
import com.cqupt.garage.pojo.GarageVehicle;
import com.cqupt.garage.pojo.User;
import com.cqupt.garage.service.GarageRecordService;
import com.cqupt.garage.service.PlateModelClient;
import com.cqupt.garage.service.PlateRecognitionService;
import com.cqupt.garage.service.PlateRecognitionWorkflowService;
import com.cqupt.garage.service.UserService;
import com.cqupt.garage.utils.DateTimeUtils;
import com.cqupt.garage.utils.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class PlateRecognitionWorkflowServiceImpl implements PlateRecognitionWorkflowService {

    @Autowired
    private UserService userService;

    @Autowired
    private PlateModelClient plateModelClient;

    @Autowired
    private PlateRecognitionService plateRecognitionService;

    @Autowired
    private GarageRecordService garageRecordService;

    @Autowired
    private GarageReservationMapper garageReservationMapper;

    @Autowired
    private GarageSpaceMapper garageSpaceMapper;

    @Autowired
    private GarageRecordMapper garageRecordMapper;

    @Autowired
    private GarageVehicleMapper garageVehicleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVo<Object> recognizeAndTransit(MultipartFile imageFile, String action, String cameraCode, String spaceNo) {
        User currentUser = userService.getCurrentLoginUser();
        if (!userService.isAdmin(currentUser)) {
            return ResultVo.fail("仅管理员可执行车牌识别自动入出库");
        }
        if (imageFile == null || imageFile.isEmpty()) {
            return ResultVo.fail("请上传车牌图片");
        }

        byte[] imageBytes;
        try {
            imageBytes = imageFile.getBytes();
        } catch (IOException ex) {
            return ResultVo.fail("读取图片失败:" + ex.getMessage());
        }

        String normalizedAction = normalizeAction(action);
        String eventTime = DateTimeUtils.nowDateTime();
        String normalizedSpaceNo = isBlank(spaceNo) ? "" : spaceNo.trim();
        String normalizedCameraCode = isBlank(cameraCode) ? "UPLOAD" : cameraCode.trim();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("request", buildRequestData(imageFile.getOriginalFilename(), normalizedAction, normalizedCameraCode, normalizedSpaceNo));

        PlateModelResultDTO modelResult = plateModelClient.recognize(imageBytes, imageFile.getOriginalFilename());
        payload.put("model", modelResult.toMap());
        if (!modelResult.isSuccess() || isBlank(modelResult.getPlateNo())) {
            return ResultVo.fail("车牌识别失败，请检查图片清晰度或命名格式", payload);
        }

        String plateNo = adaptPlateNoForBusiness(modelResult.getPlateNo());
        PlateRecognitionEventDTO event = new PlateRecognitionEventDTO();
        event.setPlateNo(plateNo);
        event.setAction(normalizedAction);
        event.setCameraCode(normalizedCameraCode);
        event.setEventTime(eventTime);
        event.setSpaceNo(normalizedSpaceNo);

        ResultVo<Object> analysisResult = plateRecognitionService.analyzePlateEvent(event);
        payload.put("analysis", buildResultSummary(analysisResult));
        if (!analysisResult.isFlag()) {
            return ResultVo.fail(analysisResult.getMessage(), payload);
        }

        if ("OUT".equals(normalizedAction)) {
            return processOut(eventTime, plateNo, analysisResult.getData(), payload);
        }
        return processIn(eventTime, plateNo, normalizedSpaceNo, analysisResult.getData(), payload);
    }

    private ResultVo<Object> processIn(String eventTime, String plateNo, String preferSpaceNo, Object analysisData, Map<String, Object> payload) {
        String operationPlateNo = resolveOperationPlateNo(plateNo, analysisData);
        String finalSpaceNo = resolveInSpaceNo(preferSpaceNo, plateNo, analysisData);
        if (isBlank(finalSpaceNo)) {
            return ResultVo.fail("识别成功但未找到可用车位", payload);
        }

        GarageRecord inCommand = new GarageRecord();
        inCommand.setPlateNo(operationPlateNo);
        inCommand.setSpaceNo(finalSpaceNo);
        inCommand.setInTime(eventTime);
        inCommand.setRemark("AI识别自动入库");
        ResultVo<Object> operateResult = garageRecordService.addGarageInRecord(inCommand);

        Map<String, Object> operationData = buildResultSummary(operateResult);
        operationData.put("action", "IN");
        operationData.put("plateNo", operationPlateNo);
        operationData.put("spaceNo", finalSpaceNo);
        payload.put("operation", operationData);
        if (!operateResult.isFlag()) {
            return ResultVo.fail(operateResult.getMessage(), payload);
        }
        return ResultVo.ok(payload, "识别成功，车辆已自动入库");
    }

    private ResultVo<Object> processOut(String eventTime, String plateNo, Object analysisData, Map<String, Object> payload) {
        Long recordId = extractLong(analysisData, "recordId", "id");
        GarageRecord activeRecord = null;
        if (recordId == null) {
            QueryWrapper<GarageRecord> recordQuery = new QueryWrapper<>();
            recordQuery.eq("plate_no", plateNo).eq("record_status", "0").orderByDesc("id").last("limit 1");
            activeRecord = garageRecordMapper.selectOne(recordQuery);
            recordId = activeRecord == null ? null : activeRecord.getId();
        }
        if (recordId == null) {
            return ResultVo.fail("识别成功但未找到在库记录", payload);
        }
        if (activeRecord == null) {
            activeRecord = garageRecordService.getById(recordId);
        }
        if (activeRecord == null) {
            return ResultVo.fail("识别成功但未找到在库记录", payload);
        }
        if (!"1".equals(activeRecord.getPayStatus())) {
            Map<String, Object> operationData = new LinkedHashMap<>();
            operationData.put("flag", false);
            operationData.put("message", "该车辆未支付，请先完成支付后再扫码出库");
            operationData.put("action", "OUT");
            operationData.put("recordId", recordId);
            operationData.put("payStatus", activeRecord.getPayStatus());
            payload.put("operation", operationData);
            return ResultVo.fail("该车辆未支付，请先完成支付后再扫码出库", payload);
        }

        GarageRecord outCommand = new GarageRecord();
        outCommand.setId(recordId);
        outCommand.setOutTime(eventTime);
        ResultVo<Object> operateResult = garageRecordService.updateGarageOutRecord(outCommand);

        Map<String, Object> operationData = buildResultSummary(operateResult);
        operationData.put("action", "OUT");
        operationData.put("recordId", recordId);
        operationData.put("payStatus", activeRecord.getPayStatus());
        payload.put("operation", operationData);
        if (!operateResult.isFlag()) {
            return ResultVo.fail(operateResult.getMessage(), payload);
        }
        return ResultVo.ok(payload, "识别成功，车辆已自动出库");
    }

    private Map<String, Object> buildRequestData(String fileName, String action, String cameraCode, String spaceNo) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("fileName", isBlank(fileName) ? "" : fileName);
        data.put("action", action);
        data.put("cameraCode", cameraCode);
        data.put("spaceNo", spaceNo);
        data.put("eventTime", DateTimeUtils.nowDateTime());
        return data;
    }

    private Map<String, Object> buildResultSummary(ResultVo<Object> result) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (result == null) {
            map.put("flag", false);
            map.put("message", "服务返回为空");
            map.put("data", "");
            return map;
        }
        map.put("flag", result.isFlag());
        map.put("message", result.getMessage());
        map.put("data", result.getData());
        return map;
    }

    private String resolveInSpaceNo(String preferSpaceNo, String plateNo, Object analysisData) {
        if (!isBlank(preferSpaceNo)) {
            return preferSpaceNo.trim();
        }
        String suggestedSpaceNo = extractString(analysisData, "suggestedSpaceNo", "spaceNo");
        if (!isBlank(suggestedSpaceNo)) {
            return suggestedSpaceNo.trim();
        }

        QueryWrapper<GarageReservation> reservationQuery = new QueryWrapper<>();
        reservationQuery.eq("plate_no", plateNo).eq("reservation_status", "0").orderByDesc("id").last("limit 1");
        GarageReservation reservation = garageReservationMapper.selectOne(reservationQuery);
        if (reservation != null && !isBlank(reservation.getSpaceNo())) {
            return reservation.getSpaceNo().trim();
        }

        QueryWrapper<GarageSpace> spaceQuery = new QueryWrapper<>();
        spaceQuery.eq("status", "0").orderByAsc("id").last("limit 1");
        GarageSpace freeSpace = garageSpaceMapper.selectOne(spaceQuery);
        if (freeSpace == null) {
            return "";
        }
        return isBlank(freeSpace.getSpaceNo()) ? "" : freeSpace.getSpaceNo().trim();
    }

    private Long extractLong(Object source, String... keys) {
        Object value = findInSource(source, keys);
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (Exception ex) {
            return null;
        }
    }

    private String extractString(Object source, String... keys) {
        Object value = findInSource(source, keys);
        return value == null ? "" : String.valueOf(value);
    }

    private Object findInSource(Object source, String... keys) {
        if (source == null || keys == null || keys.length == 0) {
            return null;
        }
        if (source instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) source;
            for (String key : keys) {
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (entry.getKey() != null && key.equalsIgnoreCase(String.valueOf(entry.getKey()))) {
                        return entry.getValue();
                    }
                }
            }
            for (Object value : map.values()) {
                Object nested = findInSource(value, keys);
                if (nested != null) {
                    return nested;
                }
            }
            return null;
        }
        return null;
    }

    private String normalizeAction(String action) {
        if (isBlank(action)) {
            return "IN";
        }
        String normalized = action.trim().toUpperCase();
        if ("IN".equals(normalized) || "OUT".equals(normalized)) {
            return normalized;
        }
        String raw = action.trim();
        if ("进入".equals(raw) || "入场".equals(raw) || "入库".equals(raw)) {
            return "IN";
        }
        if ("离开".equals(raw) || "出场".equals(raw) || "出库".equals(raw)) {
            return "OUT";
        }
        return "IN";
    }

    private String resolveOperationPlateNo(String fallbackPlateNo, Object analysisData) {
        String plateFromAnalysis = extractString(analysisData, "plateNo");
        if (!isBlank(plateFromAnalysis)) {
            return plateFromAnalysis.trim().toUpperCase();
        }
        return isBlank(fallbackPlateNo) ? "" : fallbackPlateNo.trim().toUpperCase();
    }

    private String adaptPlateNoForBusiness(String rawPlateNo) {
        if (isBlank(rawPlateNo)) {
            return "";
        }
        String plateNo = rawPlateNo.trim().toUpperCase();
        if (existsVehiclePlate(plateNo)) {
            return plateNo;
        }

        String noSeparator = stripPlateSeparators(plateNo);
        if (existsVehiclePlate(noSeparator)) {
            return noSeparator;
        }

        String withDash = appendDashPlate(noSeparator);
        if (!isBlank(withDash) && existsVehiclePlate(withDash)) {
            return withDash;
        }

        String withMiddleDot = appendMiddleDotPlate(noSeparator);
        if (!isBlank(withMiddleDot) && existsVehiclePlate(withMiddleDot)) {
            return withMiddleDot;
        }

        return noSeparator;
    }

    private boolean existsVehiclePlate(String plateNo) {
        if (isBlank(plateNo)) {
            return false;
        }
        QueryWrapper<GarageVehicle> vehicleQuery = new QueryWrapper<>();
        vehicleQuery.eq("plate_no", plateNo).last("limit 1");
        return garageVehicleMapper.selectOne(vehicleQuery) != null;
    }

    private String appendDashPlate(String normalizedPlateNo) {
        if (isBlank(normalizedPlateNo) || normalizedPlateNo.length() < 7 || normalizedPlateNo.contains("-")) {
            return normalizedPlateNo;
        }
        return normalizedPlateNo.substring(0, 2) + "-" + normalizedPlateNo.substring(2);
    }

    private String appendMiddleDotPlate(String normalizedPlateNo) {
        if (isBlank(normalizedPlateNo) || normalizedPlateNo.length() < 7) {
            return normalizedPlateNo;
        }
        if (normalizedPlateNo.contains("\u00B7") || normalizedPlateNo.contains("\u2022") || normalizedPlateNo.contains("\u30FB")) {
            return normalizedPlateNo;
        }
        return normalizedPlateNo.substring(0, 2) + "\u00B7" + normalizedPlateNo.substring(2);
    }

    private String stripPlateSeparators(String plateNo) {
        if (isBlank(plateNo)) {
            return "";
        }
        return plateNo.trim().toUpperCase().replaceAll("[\\s\\-\\u00B7\\u2022\\u30FB\\.\\u3002_]", "");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
