package com.cqupt.garage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cqupt.garage.dto.PlateRecognitionEventDTO;
import com.cqupt.garage.mapper.DriverProfileMapper;
import com.cqupt.garage.mapper.GarageRecordMapper;
import com.cqupt.garage.mapper.GarageReservationMapper;
import com.cqupt.garage.mapper.GarageSpaceMapper;
import com.cqupt.garage.mapper.GarageVehicleMapper;
import com.cqupt.garage.pojo.DriverProfile;
import com.cqupt.garage.pojo.GarageRecord;
import com.cqupt.garage.pojo.GarageReservation;
import com.cqupt.garage.pojo.GarageSpace;
import com.cqupt.garage.pojo.GarageVehicle;
import com.cqupt.garage.service.FeeRuleService;
import com.cqupt.garage.service.PlateRecognitionService;
import com.cqupt.garage.utils.DateTimeUtils;
import com.cqupt.garage.utils.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlateRecognitionServiceImpl implements PlateRecognitionService {

    @Autowired
    private GarageVehicleMapper garageVehicleMapper;

    @Autowired
    private DriverProfileMapper driverProfileMapper;

    @Autowired
    private GarageReservationMapper garageReservationMapper;

    @Autowired
    private GarageRecordMapper garageRecordMapper;

    @Autowired
    private GarageSpaceMapper garageSpaceMapper;

    @Autowired
    private FeeRuleService feeRuleService;

    @Override
    public ResultVo<Object> analyzePlateEvent(PlateRecognitionEventDTO dto) {
        if (dto == null || isBlank(dto.getPlateNo())) {
            return ResultVo.fail("车牌号不能为空");
        }
        String plateNo = dto.getPlateNo().trim().toUpperCase();
        String action = normalizeAction(dto.getAction());
        if (isBlank(action)) {
            return ResultVo.fail("action 仅支持 IN 或 OUT");
        }
        String eventTime = DateTimeUtils.isValidDateTime(dto.getEventTime()) ? dto.getEventTime().trim() : DateTimeUtils.nowDateTime();

        if ("OUT".equals(action)) {
            return analyzeOut(plateNo, eventTime, dto.getCameraCode());
        }
        return analyzeIn(plateNo, eventTime, dto.getCameraCode(), dto.getSpaceNo());
    }

    private ResultVo<Object> analyzeIn(String plateNo, String eventTime, String cameraCode, String preferSpaceNo) {
        GarageVehicle vehicle = findVehicleByPlateFlexible(plateNo);
        if (vehicle == null) {
            Map<String, Object> data = buildBaseData(plateNo, "IN", eventTime, cameraCode);
            data.put("nextStep", "REGISTER_VEHICLE");
            return ResultVo.fail("未匹配到已登记车辆", data);
        }
        String businessPlateNo = canonicalPlateNo(vehicle.getPlateNo(), plateNo);

        if ("2".equals(vehicle.getStatus())) {
            return ResultVo.fail("该车辆已停用");
        }

        QueryWrapper<DriverProfile> profileQuery = new QueryWrapper<>();
        profileQuery.eq("user_id", vehicle.getUserId());
        profileQuery.eq("status", "1");
        if (driverProfileMapper.selectCount(profileQuery) <= 0) {
            return ResultVo.fail("车主未维护有效驾驶档案");
        }

        if (findActiveRecordByPlateFlexible(businessPlateNo) != null) {
            return ResultVo.fail("该车辆当前已在库");
        }

        GarageReservation activeReservation = findActiveReservationByPlateFlexible(businessPlateNo);
        Map<String, Object> data = buildBaseData(businessPlateNo, "IN", eventTime, cameraCode);
        data.put("vehicleId", vehicle.getId());
        data.put("userId", vehicle.getUserId());
        if (activeReservation != null) {
            data.put("mode", "IN_BY_RESERVATION");
            data.put("reservationId", activeReservation.getId());
            data.put("suggestedSpaceNo", activeReservation.getSpaceNo());
            data.put("nextStep", "ADMIN_CHECK_IN_RESERVATION");
            return ResultVo.ok(data, "识别成功，建议管理员执行预约转入库");
        }

        GarageSpace space = findAvailableSpace(preferSpaceNo);
        if (space == null) {
            data.put("mode", "IN_WAITING_SPACE");
            data.put("nextStep", "ALLOCATE_SPACE");
            return ResultVo.fail("未找到可用车位", data);
        }
        data.put("mode", "IN_DIRECT");
        data.put("suggestedSpaceNo", space.getSpaceNo());
        data.put("nextStep", "ADMIN_CREATE_IN_RECORD");
        return ResultVo.ok(data, "识别成功，可执行入库");
    }

    private ResultVo<Object> analyzeOut(String plateNo, String eventTime, String cameraCode) {
        GarageRecord activeRecord = findActiveRecordByPlateFlexible(plateNo);
        if (activeRecord == null) {
            Map<String, Object> data = buildBaseData(plateNo, "OUT", eventTime, cameraCode);
            data.put("nextStep", "NO_ACTIVE_RECORD");
            return ResultVo.fail("未匹配到在库记录", data);
        }

        long parkingMinutes = DateTimeUtils.diffMinutes(activeRecord.getInTime(), eventTime);
        String estimatedFee = feeRuleService.calcFeeByMinutes(parkingMinutes);
        Map<String, Object> data = buildBaseData(canonicalPlateNo(activeRecord.getPlateNo(), plateNo), "OUT", eventTime, cameraCode);
        data.put("mode", "OUT_NEED_PAYMENT");
        data.put("recordId", activeRecord.getId());
        data.put("spaceNo", activeRecord.getSpaceNo());
        data.put("inTime", activeRecord.getInTime());
        data.put("estimatedParkingMinutes", String.valueOf(parkingMinutes));
        data.put("estimatedFee", estimatedFee);
        data.put("payStatus", activeRecord.getPayStatus());
        if ("1".equals(activeRecord.getPayStatus())) {
            data.put("mode", "OUT_READY_CHECKOUT");
            data.put("nextStep", "AUTO_CHECK_OUT");
            return ResultVo.ok(data, "识别成功，车辆已支付，可直接扫码出库");
        }
        data.put("nextStep", "CREATE_CHECKOUT_ORDER");
        return ResultVo.ok(data, "识别成功，建议先完成支付再出库");
    }

    private GarageSpace findAvailableSpace(String preferSpaceNo) {
        if (!isBlank(preferSpaceNo)) {
            QueryWrapper<GarageSpace> preferQuery = new QueryWrapper<>();
            preferQuery.eq("space_no", preferSpaceNo.trim()).eq("status", "0").last("limit 1");
            GarageSpace prefer = garageSpaceMapper.selectOne(preferQuery);
            if (prefer != null) {
                return prefer;
            }
        }
        QueryWrapper<GarageSpace> freeQuery = new QueryWrapper<>();
        freeQuery.eq("status", "0").orderByAsc("id").last("limit 1");
        return garageSpaceMapper.selectOne(freeQuery);
    }

    private GarageVehicle findVehicleByPlateFlexible(String plateNo) {
        if (isBlank(plateNo)) {
            return null;
        }
        String raw = plateNo.trim().toUpperCase();
        QueryWrapper<GarageVehicle> exactQuery = new QueryWrapper<>();
        exactQuery.eq("plate_no", raw).orderByDesc("id").last("limit 1");
        GarageVehicle exactVehicle = garageVehicleMapper.selectOne(exactQuery);
        if (exactVehicle != null) {
            return exactVehicle;
        }

        String normalizedTarget = normalizePlateNo(raw);
        if (isBlank(normalizedTarget)) {
            return null;
        }
        List<GarageVehicle> vehicles = garageVehicleMapper.selectList(new QueryWrapper<GarageVehicle>().orderByDesc("id"));
        for (GarageVehicle item : vehicles) {
            if (item == null || isBlank(item.getPlateNo())) {
                continue;
            }
            if (normalizedTarget.equals(normalizePlateNo(item.getPlateNo()))) {
                return item;
            }
        }
        return null;
    }

    private GarageRecord findActiveRecordByPlateFlexible(String plateNo) {
        if (isBlank(plateNo)) {
            return null;
        }
        String raw = plateNo.trim().toUpperCase();
        QueryWrapper<GarageRecord> exactQuery = new QueryWrapper<>();
        exactQuery.eq("plate_no", raw).eq("record_status", "0").orderByDesc("id").last("limit 1");
        GarageRecord exactRecord = garageRecordMapper.selectOne(exactQuery);
        if (exactRecord != null) {
            return exactRecord;
        }

        String normalizedTarget = normalizePlateNo(raw);
        List<GarageRecord> activeRecords = garageRecordMapper.selectList(
                new QueryWrapper<GarageRecord>().eq("record_status", "0").orderByDesc("id"));
        for (GarageRecord item : activeRecords) {
            if (item == null || isBlank(item.getPlateNo())) {
                continue;
            }
            if (normalizedTarget.equals(normalizePlateNo(item.getPlateNo()))) {
                return item;
            }
        }
        return null;
    }

    private GarageReservation findActiveReservationByPlateFlexible(String plateNo) {
        if (isBlank(plateNo)) {
            return null;
        }
        String raw = plateNo.trim().toUpperCase();
        QueryWrapper<GarageReservation> exactQuery = new QueryWrapper<>();
        exactQuery.eq("plate_no", raw).eq("reservation_status", "0").orderByDesc("id").last("limit 1");
        GarageReservation exactReservation = garageReservationMapper.selectOne(exactQuery);
        if (exactReservation != null) {
            return exactReservation;
        }

        String normalizedTarget = normalizePlateNo(raw);
        List<GarageReservation> activeReservations = garageReservationMapper.selectList(
                new QueryWrapper<GarageReservation>().eq("reservation_status", "0").orderByDesc("id"));
        for (GarageReservation item : activeReservations) {
            if (item == null || isBlank(item.getPlateNo())) {
                continue;
            }
            if (normalizedTarget.equals(normalizePlateNo(item.getPlateNo()))) {
                return item;
            }
        }
        return null;
    }

    private String canonicalPlateNo(String primary, String fallback) {
        if (!isBlank(primary)) {
            return primary.trim().toUpperCase();
        }
        return isBlank(fallback) ? "" : fallback.trim().toUpperCase();
    }

    private String normalizePlateNo(String plateNo) {
        if (isBlank(plateNo)) {
            return "";
        }
        return plateNo.trim().toUpperCase().replaceAll("[\\s\\-·•・\\.。_]", "");
    }

    private Map<String, Object> buildBaseData(String plateNo, String action, String eventTime, String cameraCode) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("plateNo", plateNo);
        data.put("action", action);
        data.put("eventTime", eventTime);
        data.put("cameraCode", isBlank(cameraCode) ? "" : cameraCode.trim());
        return data;
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
        return "";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
