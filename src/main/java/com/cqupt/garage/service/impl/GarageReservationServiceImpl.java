package com.cqupt.garage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqupt.garage.dto.GarageReservationDTO;
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
import com.cqupt.garage.service.GarageReservationService;
import com.cqupt.garage.service.UserService;
import com.cqupt.garage.utils.DateTimeUtils;
import com.cqupt.garage.utils.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class GarageReservationServiceImpl extends ServiceImpl<GarageReservationMapper, GarageReservation>
        implements GarageReservationService {

    private static final int DEFAULT_PAGE_SIZE = 6;

    @Autowired
    private UserService userService;

    @Autowired
    private GarageSpaceMapper garageSpaceMapper;

    @Autowired
    private GarageVehicleMapper garageVehicleMapper;

    @Autowired
    private GarageRecordMapper garageRecordMapper;

    @Autowired
    private DriverProfileMapper driverProfileMapper;

    @Override
    public ResultVo<Object> listGarageReservationPage(GarageReservationDTO dto) {
        User currentUser = userService.getCurrentLoginUser();
        QueryWrapper<GarageReservation> queryWrapper = new QueryWrapper<>();
        if (dto != null && !isBlank(dto.getPlateNo())) {
            queryWrapper.like("plate_no", dto.getPlateNo().trim());
        }
        if (dto != null && !isBlank(dto.getSpaceNo())) {
            queryWrapper.like("space_no", dto.getSpaceNo().trim());
        }
        if (dto != null && !isBlank(dto.getReservationStatus())) {
            queryWrapper.eq("reservation_status", dto.getReservationStatus().trim());
        }
        if (!userService.isAdmin(currentUser)) {
            queryWrapper.eq("user_id", currentUser.getId());
            queryWrapper.orderByDesc("id");
        } else {
            // Admin reservation management should surface pending records first.
            queryWrapper.orderByAsc("reservation_status").orderByDesc("id");
        }
        Page<GarageReservation> page = new Page<>(resolveCurrentPage(dto == null ? null : dto.getPageSize()), DEFAULT_PAGE_SIZE);
        page(page, queryWrapper);
        return ResultVo.ok(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVo<Object> createReservation(GarageReservation garageReservation) {
        User currentUser = userService.getCurrentLoginUser();
        if (garageReservation == null || isBlank(garageReservation.getSpaceNo())) {
            return ResultVo.fail("车位编号不能为空");
        }

        String plateNo = normalizePlateNo(garageReservation.getPlateNo());
        String spaceNo = garageReservation.getSpaceNo().trim();
        String remark = isBlank(garageReservation.getRemark()) ? null : garageReservation.getRemark().trim();
        if (remark != null && remark.length() > 100) {
            return ResultVo.fail("备注长度不能超过100");
        }

        GarageVehicle vehicle = null;
        if (garageReservation.getVehicleId() != null) {
            vehicle = garageVehicleMapper.selectById(garageReservation.getVehicleId());
        }
        if (vehicle == null && !isBlank(plateNo)) {
            QueryWrapper<GarageVehicle> vehicleWrapper = new QueryWrapper<>();
            vehicleWrapper.eq("plate_no", plateNo);
            vehicle = garageVehicleMapper.selectOne(vehicleWrapper);
        }
        if (vehicle == null) {
            return ResultVo.fail("车辆不存在");
        }
        if (!userService.isAdmin(currentUser) && !currentUser.getId().equals(vehicle.getUserId())) {
            return ResultVo.fail("无权限操作");
        }
        if ("2".equals(vehicle.getStatus())) {
            return ResultVo.fail("车辆已停用");
        }
        if (!hasActiveDriverProfile(vehicle.getUserId())) {
            return ResultVo.fail("请先完善驾驶档案后再预约");
        }
        plateNo = normalizePlateNo(vehicle.getPlateNo());

        QueryWrapper<GarageSpace> spaceWrapper = new QueryWrapper<>();
        spaceWrapper.eq("space_no", spaceNo);
        GarageSpace space = garageSpaceMapper.selectOne(spaceWrapper);
        if (space == null) {
            return ResultVo.fail("车位不存在");
        }
        if (!"0".equals(space.getStatus())) {
            return ResultVo.fail("车位不可预约");
        }

        QueryWrapper<GarageReservation> activeSpaceReservation = new QueryWrapper<>();
        activeSpaceReservation.eq("space_no", spaceNo).eq("reservation_status", "0");
        if (count(activeSpaceReservation) > 0) {
            return ResultVo.fail("该车位已有进行中的预约");
        }

        QueryWrapper<GarageReservation> activeVehicleReservation = new QueryWrapper<>();
        activeVehicleReservation.eq("plate_no", plateNo).eq("reservation_status", "0");
        if (count(activeVehicleReservation) > 0) {
            return ResultVo.fail("该车辆已有进行中的预约");
        }

        QueryWrapper<GarageRecord> activeRecordQuery = new QueryWrapper<>();
        activeRecordQuery.eq("plate_no", plateNo).eq("record_status", "0");
        if (garageRecordMapper.selectCount(activeRecordQuery) > 0) {
            return ResultVo.fail("该车辆当前已在库");
        }

        GarageReservation saveReservation = new GarageReservation();
        saveReservation.setUserId(vehicle.getUserId());
        saveReservation.setVehicleId(vehicle.getId());
        saveReservation.setPlateNo(plateNo);
        saveReservation.setSpaceNo(spaceNo);
        saveReservation.setReservationTime(DateTimeUtils.nowDateTime());
        saveReservation.setReservationStatus("0");
        saveReservation.setRemark(remark);
        saveReservation.setCreateTime(LocalDateTime.now());
        saveReservation.setUpdateTime(LocalDateTime.now());
        save(saveReservation);

        space.setStatus("4");
        space.setUpdateTime(LocalDateTime.now());
        garageSpaceMapper.updateById(space);
        return ResultVo.ok("预约成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVo<Object> cancelReservation(Long id) {
        User currentUser = userService.getCurrentLoginUser();
        if (id == null) {
            return ResultVo.fail("预约ID不能为空");
        }
        GarageReservation reservation = getById(id);
        if (reservation == null) {
            return ResultVo.fail("预约不存在");
        }
        if (!"0".equals(reservation.getReservationStatus())) {
            return ResultVo.fail("预约状态不可操作");
        }
        if (!userService.isAdmin(currentUser) && !currentUser.getId().equals(reservation.getUserId())) {
            return ResultVo.fail("无权限操作");
        }

        reservation.setReservationStatus("1");
        reservation.setCancelTime(DateTimeUtils.nowDateTime());
        reservation.setUpdateTime(LocalDateTime.now());
        updateById(reservation);

        releaseSpaceIfPossible(reservation.getSpaceNo());
        return ResultVo.ok("预约已取消");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVo<Object> checkInReservation(Long id) {
        User currentUser = userService.getCurrentLoginUser();
        if (id == null) {
            return ResultVo.fail("预约ID不能为空");
        }
        if (!userService.isAdmin(currentUser)) {
            return ResultVo.fail("仅管理员可执行转入库操作");
        }
        GarageReservation reservation = getById(id);
        if (reservation == null) {
            return ResultVo.fail("预约不存在");
        }
        if (!"0".equals(reservation.getReservationStatus())) {
            return ResultVo.fail("预约状态不可操作");
        }

        QueryWrapper<GarageSpace> spaceWrapper = new QueryWrapper<>();
        spaceWrapper.eq("space_no", reservation.getSpaceNo());
        GarageSpace space = garageSpaceMapper.selectOne(spaceWrapper);
        if (space == null) {
            return ResultVo.fail("车位不存在");
        }
        if (!"4".equals(space.getStatus()) && !"0".equals(space.getStatus())) {
            return ResultVo.fail("当前车位状态不可入库");
        }

        QueryWrapper<GarageRecord> activeRecordQuery = new QueryWrapper<>();
        activeRecordQuery.eq("plate_no", reservation.getPlateNo()).eq("record_status", "0");
        if (garageRecordMapper.selectCount(activeRecordQuery) > 0) {
            return ResultVo.fail("该车辆当前已在库");
        }

        GarageRecord saveRecord = new GarageRecord();
        saveRecord.setUserId(reservation.getUserId());
        saveRecord.setPlateNo(reservation.getPlateNo());
        saveRecord.setSpaceNo(reservation.getSpaceNo());
        saveRecord.setInTime(DateTimeUtils.nowDateTime());
        saveRecord.setPayStatus("0");
        saveRecord.setRecordStatus("0");
        saveRecord.setRemark("来自预约 #" + reservation.getId());
        saveRecord.setCreateTime(LocalDateTime.now());
        saveRecord.setUpdateTime(LocalDateTime.now());
        garageRecordMapper.insert(saveRecord);

        reservation.setReservationStatus("2");
        reservation.setCheckInTime(DateTimeUtils.nowDateTime());
        reservation.setUpdateTime(LocalDateTime.now());
        updateById(reservation);

        space.setStatus("1");
        space.setUpdateTime(LocalDateTime.now());
        garageSpaceMapper.updateById(space);
        return ResultVo.ok("入库成功");
    }

    private void releaseSpaceIfPossible(String spaceNo) {
        if (isBlank(spaceNo)) {
            return;
        }
        QueryWrapper<GarageRecord> activeRecordQuery = new QueryWrapper<>();
        activeRecordQuery.eq("space_no", spaceNo).eq("record_status", "0");
        if (garageRecordMapper.selectCount(activeRecordQuery) > 0) {
            return;
        }
        QueryWrapper<GarageReservation> activeReservationQuery = new QueryWrapper<>();
        activeReservationQuery.eq("space_no", spaceNo).eq("reservation_status", "0");
        if (count(activeReservationQuery) > 0) {
            return;
        }
        QueryWrapper<GarageSpace> spaceWrapper = new QueryWrapper<>();
        spaceWrapper.eq("space_no", spaceNo);
        GarageSpace space = garageSpaceMapper.selectOne(spaceWrapper);
        if (space != null) {
            space.setStatus("0");
            space.setUpdateTime(LocalDateTime.now());
            garageSpaceMapper.updateById(space);
        }
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

    private String normalizePlateNo(String plateNo) {
        if (isBlank(plateNo)) {
            return "";
        }
        return plateNo.trim().toUpperCase();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
