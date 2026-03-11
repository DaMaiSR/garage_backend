package com.cqupt.garage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqupt.garage.dto.GarageRecordDTO;
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
        if (garageRecord == null || isBlank(garageRecord.getPlateNo()) || isBlank(garageRecord.getSpaceNo())) {
            return ResultVo.fail("plateNo and spaceNo are required");
        }

        String plateNo = garageRecord.getPlateNo().trim();
        String spaceNo = garageRecord.getSpaceNo().trim();

        QueryWrapper<GarageSpace> spaceQuery = new QueryWrapper<>();
        spaceQuery.eq("space_no", spaceNo);
        GarageSpace garageSpace = garageSpaceMapper.selectOne(spaceQuery);
        if (garageSpace == null) {
            return ResultVo.fail("space not exists");
        }

        QueryWrapper<GarageReservation> activeReservationQuery = new QueryWrapper<>();
        activeReservationQuery.eq("space_no", spaceNo).eq("reservation_status", "0").orderByDesc("id").last("limit 1");
        GarageReservation activeReservation = garageReservationMapper.selectOne(activeReservationQuery);

        if ("4".equals(garageSpace.getStatus())) {
            if (activeReservation == null) {
                return ResultVo.fail("space status invalid");
            }
            if (!userService.isAdmin(currentUser) && !currentUser.getId().equals(activeReservation.getUserId())) {
                return ResultVo.fail("space reserved by other user");
            }
            if (!plateNo.equals(activeReservation.getPlateNo())) {
                return ResultVo.fail("plate does not match reservation");
            }
        } else if (!"0".equals(garageSpace.getStatus())) {
            return ResultVo.fail("space is not available");
        }

        QueryWrapper<GarageRecord> activeRecordQuery = new QueryWrapper<>();
        activeRecordQuery.eq("plate_no", plateNo);
        activeRecordQuery.eq("record_status", "0");
        if (count(activeRecordQuery) > 0) {
            return ResultVo.fail("vehicle already in parking");
        }

        QueryWrapper<GarageVehicle> vehicleQuery = new QueryWrapper<>();
        vehicleQuery.eq("plate_no", plateNo);
        GarageVehicle garageVehicle = garageVehicleMapper.selectOne(vehicleQuery);
        if (garageVehicle == null) {
            return ResultVo.fail("vehicle not exists");
        }
        if (!userService.isAdmin(currentUser) && !currentUser.getId().equals(garageVehicle.getUserId())) {
            return ResultVo.fail("no permission");
        }

        GarageRecord saveRecord = new GarageRecord();
        saveRecord.setUserId(garageVehicle.getUserId());
        saveRecord.setPlateNo(plateNo);
        saveRecord.setSpaceNo(spaceNo);
        saveRecord.setInTime(isBlank(garageRecord.getInTime()) ? DateTimeUtils.nowDateTime() : garageRecord.getInTime());
        saveRecord.setOutTime(null);
        saveRecord.setParkingMinutes(null);
        saveRecord.setTotalFee(null);
        saveRecord.setPayStatus("0");
        saveRecord.setRecordStatus("0");
        saveRecord.setRemark(garageRecord.getRemark());
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
        garageSpace.setOwnerUserId(garageVehicle.getUserId());
        garageSpace.setUpdateTime(LocalDateTime.now());
        garageSpaceMapper.updateById(garageSpace);
        return ResultVo.ok("check in success");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVo<Object> updateGarageOutRecord(GarageRecord garageRecord) {
        User currentUser = userService.getCurrentLoginUser();
        if (garageRecord == null || garageRecord.getId() == null) {
            return ResultVo.fail("record id is required");
        }

        GarageRecord dbRecord = getById(garageRecord.getId());
        if (dbRecord == null) {
            return ResultVo.fail("record not exists");
        }
        if (!userService.isAdmin(currentUser) && !currentUser.getId().equals(dbRecord.getUserId())) {
            return ResultVo.fail("no permission");
        }
        if ("1".equals(dbRecord.getRecordStatus())) {
            return ResultVo.fail("record already checked out");
        }

        String outTime = isBlank(garageRecord.getOutTime()) ? DateTimeUtils.nowDateTime() : garageRecord.getOutTime();
        long parkingMinutes = DateTimeUtils.diffMinutes(dbRecord.getInTime(), outTime);
        String totalFee = isBlank(garageRecord.getTotalFee())
                ? DateTimeUtils.calcFeeByMinutes(parkingMinutes)
                : garageRecord.getTotalFee().trim();

        dbRecord.setOutTime(outTime);
        dbRecord.setParkingMinutes(String.valueOf(parkingMinutes));
        dbRecord.setTotalFee(totalFee);
        dbRecord.setPayStatus(isBlank(garageRecord.getPayStatus()) ? "1" : garageRecord.getPayStatus().trim());
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

        return ResultVo.ok("check out success");
    }

    private long resolveCurrentPage(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 1L;
        }
        return pageSize;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
