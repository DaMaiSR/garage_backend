package com.cqupt.garage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqupt.garage.dto.GarageRecordDTO;
import com.cqupt.garage.mapper.DriverProfileMapper;
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
import com.cqupt.garage.service.UserService;
import com.cqupt.garage.utils.DateTimeUtils;
import com.cqupt.garage.utils.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class GarageRecordServiceImpl extends ServiceImpl<GarageRecordMapper, GarageRecord> implements GarageRecordService {

    private static final int DEFAULT_PAGE_SIZE = 6;

    @Autowired
    private UserService userService;

    @Autowired
    private GarageSpaceMapper garageSpaceMapper;

    @Autowired
    private GarageVehicleMapper garageVehicleMapper;

    @Autowired
    private GarageReservationMapper garageReservationMapper;

    @Autowired
    private DriverProfileMapper driverProfileMapper;

    @Override
    public ResultVo<Object> listGarageRecordPage(GarageRecordDTO dto) {
        User currentUser = userService.getCurrentLoginUser();
        QueryWrapper<GarageRecord> queryWrapper = new QueryWrapper<>();
        if (dto != null && !isBlank(dto.getPlateNo())) {
            queryWrapper.like("plate_no", dto.getPlateNo().trim());
        }
        if (dto != null && !isBlank(dto.getRecordStatus())) {
            queryWrapper.eq("record_status", dto.getRecordStatus().trim());
        }
        if (!userService.isAdmin(currentUser)) {
            queryWrapper.eq("user_id", currentUser.getId());
        }
        queryWrapper.orderByDesc("id");
        Page<GarageRecord> page = new Page<>(resolveCurrentPage(dto == null ? null : dto.getPageSize()), DEFAULT_PAGE_SIZE);
        page(page, queryWrapper);
        return ResultVo.ok(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVo<Object> addGarageInRecord(GarageRecord garageRecord) {
        User currentUser = userService.getCurrentLoginUser();
        if (garageRecord == null || isBlank(garageRecord.getPlateNo())) {
            return ResultVo.fail("车牌号不能为空");
        }

        String plateNo = normalizePlateNo(garageRecord.getPlateNo());
        String requestSpaceNo = isBlank(garageRecord.getSpaceNo()) ? "" : garageRecord.getSpaceNo().trim();
        String inTime = isBlank(garageRecord.getInTime()) ? DateTimeUtils.nowDateTime() : garageRecord.getInTime().trim();
        String remark = isBlank(garageRecord.getRemark()) ? null : garageRecord.getRemark().trim();
        if (!DateTimeUtils.isValidDateTime(inTime)) {
            return ResultVo.fail("入场时间格式不正确");
        }
        if (remark != null && remark.length() > 100) {
            return ResultVo.fail("备注长度不能超过100");
        }

        QueryWrapper<GarageRecord> activeRecordQuery = new QueryWrapper<>();
        activeRecordQuery.eq("plate_no", plateNo);
        activeRecordQuery.eq("record_status", "0");
        if (count(activeRecordQuery) > 0) {
            return ResultVo.fail("该车辆当前已在库");
        }

        QueryWrapper<GarageVehicle> vehicleQuery = new QueryWrapper<>();
        vehicleQuery.eq("plate_no", plateNo);
        GarageVehicle garageVehicle = garageVehicleMapper.selectOne(vehicleQuery);
        if (garageVehicle == null) {
            return ResultVo.fail("车辆不存在");
        }
        if (!userService.isAdmin(currentUser) && !currentUser.getId().equals(garageVehicle.getUserId())) {
            return ResultVo.fail("无权限操作");
        }
        if ("2".equals(garageVehicle.getStatus())) {
            return ResultVo.fail("车辆已停用");
        }
        if (!hasActiveDriverProfile(garageVehicle.getUserId())) {
            return ResultVo.fail("请先完善驾驶档案后再入库");
        }

        String resolvedSpaceNo = requestSpaceNo;
        if (isBlank(resolvedSpaceNo)) {
            resolvedSpaceNo = resolveAutoInSpaceNo(plateNo, garageVehicle);
        }
        if (isBlank(resolvedSpaceNo)) {
            return ResultVo.fail("当前没有可用车位");
        }

        QueryWrapper<GarageSpace> spaceQuery = new QueryWrapper<>();
        spaceQuery.eq("space_no", resolvedSpaceNo);
        GarageSpace garageSpace = garageSpaceMapper.selectOne(spaceQuery);
        if (garageSpace == null) {
            return ResultVo.fail("车位不存在");
        }

        QueryWrapper<GarageReservation> activeReservationQuery = new QueryWrapper<>();
        activeReservationQuery.eq("space_no", resolvedSpaceNo).eq("reservation_status", "0").orderByDesc("id").last("limit 1");
        GarageReservation activeReservation = garageReservationMapper.selectOne(activeReservationQuery);
        if (activeReservation != null && !plateNo.equals(normalizePlateNo(activeReservation.getPlateNo()))) {
            return ResultVo.fail("该车位已被其他预约占用");
        }

        if ("4".equals(garageSpace.getStatus())) {
            if (activeReservation == null) {
                return ResultVo.fail("车位状态异常");
            }
            if (!userService.isAdmin(currentUser) && !currentUser.getId().equals(activeReservation.getUserId())) {
                return ResultVo.fail("该车位已被其他用户预约");
            }
            if (!plateNo.equals(normalizePlateNo(activeReservation.getPlateNo()))) {
                return ResultVo.fail("车牌号与预约信息不一致");
            }
        } else if (!"0".equals(garageSpace.getStatus())) {
            return ResultVo.fail("车位不可用");
        }

        GarageRecord saveRecord = new GarageRecord();
        saveRecord.setUserId(garageVehicle.getUserId());
        saveRecord.setPlateNo(plateNo);
        saveRecord.setSpaceNo(resolvedSpaceNo);
        saveRecord.setInTime(inTime);
        saveRecord.setOutTime(null);
        saveRecord.setParkingMinutes(null);
        saveRecord.setTotalFee(null);
        saveRecord.setPayStatus("0");
        saveRecord.setRecordStatus("0");
        saveRecord.setRemark(remark);
        saveRecord.setCreateTime(LocalDateTime.now());
        saveRecord.setUpdateTime(LocalDateTime.now());
        save(saveRecord);

        if (activeReservation != null) {
            activeReservation.setReservationStatus("2");
            activeReservation.setCheckInTime(DateTimeUtils.nowDateTime());
            activeReservation.setUpdateTime(LocalDateTime.now());
            garageReservationMapper.updateById(activeReservation);
        }

        garageSpace.setStatus("1");
        garageSpace.setUpdateTime(LocalDateTime.now());
        garageSpaceMapper.updateById(garageSpace);
        if (isBlank(requestSpaceNo)) {
            return ResultVo.ok("入库成功，已自动分配车位:" + resolvedSpaceNo);
        }
        return ResultVo.ok("入库成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVo<Object> payGarageOutRecord(GarageRecord garageRecord) {
        User currentUser = userService.getCurrentLoginUser();
        if (garageRecord == null || garageRecord.getId() == null) {
            return ResultVo.fail("记录ID不能为空");
        }

        GarageRecord dbRecord = getById(garageRecord.getId());
        if (dbRecord == null) {
            return ResultVo.fail("记录不存在");
        }
        if (!userService.isAdmin(currentUser) && !currentUser.getId().equals(dbRecord.getUserId())) {
            return ResultVo.fail("无权限操作");
        }
        if ("1".equals(dbRecord.getRecordStatus())) {
            return ResultVo.fail("记录已完成出库");
        }
        if ("1".equals(dbRecord.getPayStatus())) {
            return ResultVo.ok("该记录已支付，无需重复支付");
        }

        String payTime = isBlank(garageRecord.getOutTime()) ? DateTimeUtils.nowDateTime() : garageRecord.getOutTime().trim();
        if (!DateTimeUtils.isValidDateTime(payTime)) {
            return ResultVo.fail("支付时间格式不正确");
        }
        if (!DateTimeUtils.isEndNotBeforeStart(dbRecord.getInTime(), payTime)) {
            return ResultVo.fail("支付时间不能早于入场时间");
        }
        long parkingMinutes = DateTimeUtils.diffMinutes(dbRecord.getInTime(), payTime);
        String totalFee = DateTimeUtils.calcFeeByMinutes(parkingMinutes);

        String payMethod = normalizePayMethod(garageRecord.getPayMethod());
        if (!isSupportedPayMethod(payMethod)) {
            return ResultVo.fail("请选择有效支付方式");
        }
        if (!"0".equals(totalFee) && "FREE".equals(payMethod)) {
            return ResultVo.fail("有费用时不能使用免单支付");
        }

        dbRecord.setParkingMinutes(String.valueOf(parkingMinutes));
        dbRecord.setTotalFee(totalFee);
        dbRecord.setPayStatus("1");
        dbRecord.setRemark(appendPayInfoRemark(dbRecord.getRemark(), payMethod, totalFee, payTime));
        dbRecord.setUpdateTime(LocalDateTime.now());
        updateById(dbRecord);
        return ResultVo.ok("支付成功，请前往出口扫码出库");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVo<Object> updateGarageOutRecord(GarageRecord garageRecord) {
        User currentUser = userService.getCurrentLoginUser();
        if (garageRecord == null || garageRecord.getId() == null) {
            return ResultVo.fail("记录ID不能为空");
        }

        GarageRecord dbRecord = getById(garageRecord.getId());
        if (dbRecord == null) {
            return ResultVo.fail("记录不存在");
        }
        if (!userService.isAdmin(currentUser) && !currentUser.getId().equals(dbRecord.getUserId())) {
            return ResultVo.fail("无权限操作");
        }
        if ("1".equals(dbRecord.getRecordStatus())) {
            return ResultVo.fail("记录已完成出库");
        }
        if (!"1".equals(dbRecord.getPayStatus())) {
            return ResultVo.fail("该车辆尚未支付，请先支付后再出库");
        }

        String outTime = isBlank(garageRecord.getOutTime()) ? DateTimeUtils.nowDateTime() : garageRecord.getOutTime().trim();
        if (!DateTimeUtils.isValidDateTime(outTime)) {
            return ResultVo.fail("出场时间格式不正确");
        }
        if (!DateTimeUtils.isEndNotBeforeStart(dbRecord.getInTime(), outTime)) {
            return ResultVo.fail("出场时间不能早于入场时间");
        }
        long parkingMinutes = DateTimeUtils.diffMinutes(dbRecord.getInTime(), outTime);

        dbRecord.setOutTime(outTime);
        if (isBlank(dbRecord.getParkingMinutes())) {
            dbRecord.setParkingMinutes(String.valueOf(parkingMinutes));
        }
        if (isBlank(dbRecord.getTotalFee())) {
            dbRecord.setTotalFee(DateTimeUtils.calcFeeByMinutes(parkingMinutes));
        }
        dbRecord.setRecordStatus("1");
        dbRecord.setUpdateTime(LocalDateTime.now());
        updateById(dbRecord);

        QueryWrapper<GarageSpace> spaceQuery = new QueryWrapper<>();
        spaceQuery.eq("space_no", dbRecord.getSpaceNo());
        GarageSpace garageSpace = garageSpaceMapper.selectOne(spaceQuery);
        if (garageSpace != null) {
            QueryWrapper<GarageReservation> reservationQuery = new QueryWrapper<>();
            reservationQuery.eq("space_no", dbRecord.getSpaceNo()).eq("reservation_status", "0");
            long activeReservationCount = garageReservationMapper.selectCount(reservationQuery);
            garageSpace.setStatus(activeReservationCount > 0 ? "4" : "0");
            garageSpace.setUpdateTime(LocalDateTime.now());
            garageSpaceMapper.updateById(garageSpace);
        }

        return ResultVo.ok("出库成功");
    }

    private long resolveCurrentPage(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 1L;
        }
        return pageSize;
    }

    private boolean hasActiveDriverProfile(Long userId) {
        if (userId == null) {
            return false;
        }
        QueryWrapper<com.cqupt.garage.pojo.DriverProfile> profileQuery = new QueryWrapper<>();
        profileQuery.eq("user_id", userId);
        profileQuery.eq("status", "1");
        return driverProfileMapper.selectCount(profileQuery) > 0;
    }

    private String resolveAutoInSpaceNo(String plateNo, GarageVehicle vehicle) {
        QueryWrapper<GarageReservation> reservationQuery = new QueryWrapper<>();
        reservationQuery.eq("plate_no", plateNo).eq("reservation_status", "0").orderByDesc("id").last("limit 1");
        GarageReservation reservation = garageReservationMapper.selectOne(reservationQuery);
        if (reservation != null && !isBlank(reservation.getSpaceNo())) {
            QueryWrapper<GarageSpace> reservedSpaceQuery = new QueryWrapper<>();
            reservedSpaceQuery.eq("space_no", reservation.getSpaceNo()).last("limit 1");
            GarageSpace reservedSpace = garageSpaceMapper.selectOne(reservedSpaceQuery);
            if (reservedSpace != null && ("4".equals(reservedSpace.getStatus()) || "0".equals(reservedSpace.getStatus()))) {
                return reservation.getSpaceNo().trim();
            }
        }

        if (vehicle != null && !isBlank(vehicle.getBindSpaceNo())) {
            QueryWrapper<GarageSpace> bindSpaceQuery = new QueryWrapper<>();
            bindSpaceQuery.eq("space_no", vehicle.getBindSpaceNo().trim()).eq("status", "0").last("limit 1");
            GarageSpace bindSpace = garageSpaceMapper.selectOne(bindSpaceQuery);
            if (bindSpace != null) {
                return bindSpace.getSpaceNo();
            }
        }

        if (isElectricVehicle(vehicle == null ? null : vehicle.getVehicleType())) {
            String chargingSpaceNo = firstFreeByType("3");
            if (!isBlank(chargingSpaceNo)) {
                return chargingSpaceNo;
            }
            String tempSpaceNo = firstFreeByType("2");
            if (!isBlank(tempSpaceNo)) {
                return tempSpaceNo;
            }
        } else {
            String tempSpaceNo = firstFreeByType("2");
            if (!isBlank(tempSpaceNo)) {
                return tempSpaceNo;
            }
        }

        String fixedSpaceNo = firstFreeByType("1");
        if (!isBlank(fixedSpaceNo)) {
            return fixedSpaceNo;
        }

        QueryWrapper<GarageSpace> anyFreeQuery = new QueryWrapper<>();
        anyFreeQuery.eq("status", "0").orderByAsc("id").last("limit 1");
        GarageSpace anyFree = garageSpaceMapper.selectOne(anyFreeQuery);
        return anyFree == null ? "" : anyFree.getSpaceNo();
    }

    private String firstFreeByType(String spaceType) {
        QueryWrapper<GarageSpace> query = new QueryWrapper<>();
        query.eq("status", "0").eq("space_type", spaceType).orderByAsc("id").last("limit 1");
        GarageSpace garageSpace = garageSpaceMapper.selectOne(query);
        return garageSpace == null ? "" : garageSpace.getSpaceNo();
    }

    private boolean isElectricVehicle(String vehicleType) {
        if (isBlank(vehicleType)) {
            return false;
        }
        String normalized = vehicleType.trim().toUpperCase();
        return normalized.contains("新能源")
                || normalized.contains("电")
                || normalized.contains("EV")
                || normalized.contains("ELECTRIC");
    }

    private String normalizePlateNo(String plateNo) {
        if (isBlank(plateNo)) {
            return "";
        }
        return plateNo.trim().toUpperCase();
    }

    private String normalizePayMethod(String payMethod) {
        if (isBlank(payMethod)) {
            return "FREE";
        }
        String method = payMethod.trim().toUpperCase();
        if ("WECHAT".equals(method) || "ALIPAY".equals(method) || "CASH".equals(method) || "FREE".equals(method)) {
            return method;
        }
        if ("微信".equals(method) || "微信支付".equals(method)) {
            return "WECHAT";
        }
        if ("支付宝".equals(method)) {
            return "ALIPAY";
        }
        if ("现金".equals(method)) {
            return "CASH";
        }
        if ("免单".equals(method)) {
            return "FREE";
        }
        return "";
    }

    private boolean isSupportedPayMethod(String payMethod) {
        return "WECHAT".equals(payMethod)
                || "ALIPAY".equals(payMethod)
                || "CASH".equals(payMethod)
                || "FREE".equals(payMethod);
    }

    private String appendPayInfoRemark(String rawRemark, String payMethod, String totalFee, String payTime) {
        String methodLabel = "其他";
        if ("WECHAT".equals(payMethod)) {
            methodLabel = "微信支付";
        } else if ("ALIPAY".equals(payMethod)) {
            methodLabel = "支付宝";
        } else if ("CASH".equals(payMethod)) {
            methodLabel = "现金";
        } else if ("FREE".equals(payMethod)) {
            methodLabel = "免单";
        }

        String payInfo = "支付方式:" + methodLabel + ", 金额:" + totalFee + "元, 时间:" + payTime;
        String origin = isBlank(rawRemark) ? "" : rawRemark.trim();
        String merged = origin.isEmpty() ? payInfo : origin + "；" + payInfo;
        return merged.length() > 255 ? merged.substring(0, 255) : merged;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
