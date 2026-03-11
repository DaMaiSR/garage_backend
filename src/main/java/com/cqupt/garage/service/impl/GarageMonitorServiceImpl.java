package com.cqupt.garage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cqupt.garage.mapper.GarageRecordMapper;
import com.cqupt.garage.mapper.GarageReservationMapper;
import com.cqupt.garage.mapper.GarageSpaceMapper;
import com.cqupt.garage.pojo.GarageRecord;
import com.cqupt.garage.pojo.GarageReservation;
import com.cqupt.garage.pojo.GarageSpace;
import com.cqupt.garage.pojo.User;
import com.cqupt.garage.service.GarageMonitorService;
import com.cqupt.garage.service.UserService;
import com.cqupt.garage.utils.DateTimeUtils;
import com.cqupt.garage.utils.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GarageMonitorServiceImpl implements GarageMonitorService {

    @Autowired
    private UserService userService;

    @Autowired
    private GarageSpaceMapper garageSpaceMapper;

    @Autowired
    private GarageRecordMapper garageRecordMapper;

    @Autowired
    private GarageReservationMapper garageReservationMapper;

    @Override
    public ResultVo<Object> getRealtimeData() {
        User currentUser = userService.getCurrentLoginUser();
        if (!userService.isAdmin(currentUser)) {
            return ResultVo.fail("admin only");
        }
        return ResultVo.ok(buildRealtimeSnapshot());
    }

    @Override
    public Map<String, Object> buildRealtimeSnapshot() {
        String now = DateTimeUtils.nowDateTime();
        List<GarageSpace> spaces = garageSpaceMapper.selectList(new QueryWrapper<GarageSpace>().orderByAsc("id"));
        List<GarageRecord> allRecords = garageRecordMapper.selectList(new QueryWrapper<GarageRecord>().orderByDesc("id"));
        List<GarageReservation> reservations = garageReservationMapper.selectList(
                new QueryWrapper<GarageReservation>().orderByDesc("id")
        );

        List<GarageRecord> activeRecords = allRecords.stream()
                .filter(record -> "0".equals(record.getRecordStatus()))
                .sorted(Comparator.comparing(GarageRecord::getId).reversed())
                .collect(Collectors.toList());

        List<GarageReservation> activeReservations = reservations.stream()
                .filter(item -> "0".equals(item.getReservationStatus()))
                .sorted(Comparator.comparing(GarageReservation::getId).reversed())
                .collect(Collectors.toList());

        List<Map<String, Object>> activeRecordRows = new ArrayList<>();
        for (GarageRecord record : activeRecords) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", record.getId());
            row.put("userId", record.getUserId());
            row.put("plateNo", record.getPlateNo());
            row.put("spaceNo", record.getSpaceNo());
            row.put("inTime", record.getInTime());
            row.put("outTime", record.getOutTime());
            row.put("parkingMinutes", record.getParkingMinutes());
            row.put("totalFee", record.getTotalFee());
            row.put("payStatus", record.getPayStatus());
            row.put("recordStatus", record.getRecordStatus());
            row.put("remark", record.getRemark());
            row.put("durationMinutes", String.valueOf(DateTimeUtils.diffMinutes(record.getInTime(), now)));
            activeRecordRows.add(row);
        }

        List<Map<String, Object>> latestRecordRows = new ArrayList<>();
        for (GarageRecord record : allRecords.stream().limit(10).collect(Collectors.toList())) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", record.getId());
            row.put("plateNo", record.getPlateNo());
            row.put("spaceNo", record.getSpaceNo());
            row.put("inTime", record.getInTime());
            row.put("outTime", record.getOutTime());
            row.put("recordStatus", record.getRecordStatus());
            row.put("totalFee", record.getTotalFee());
            latestRecordRows.add(row);
        }

        Map<String, String> spaceNoToPlate = new HashMap<>();
        for (GarageRecord record : activeRecords) {
            if (!spaceNoToPlate.containsKey(record.getSpaceNo())) {
                spaceNoToPlate.put(record.getSpaceNo(), record.getPlateNo());
            }
        }

        List<Map<String, Object>> spaceRows = new ArrayList<>();
        for (GarageSpace space : spaces) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", space.getId());
            row.put("areaName", space.getAreaName());
            row.put("floorNo", space.getFloorNo());
            row.put("spaceNo", space.getSpaceNo());
            row.put("spaceType", space.getSpaceType());
            row.put("status", space.getStatus());
            row.put("ownerUserId", space.getOwnerUserId());
            row.put("remark", space.getRemark());
            row.put("currentPlateNo", spaceNoToPlate.getOrDefault(space.getSpaceNo(), ""));
            spaceRows.add(row);
        }

        String todayPrefix = now.substring(0, 10);
        long inTodayCount = allRecords.stream().filter(record -> record.getInTime() != null && record.getInTime().startsWith(todayPrefix)).count();
        long outTodayCount = allRecords.stream().filter(record -> record.getOutTime() != null && record.getOutTime().startsWith(todayPrefix)).count();

        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("totalSpaceCount", spaces.size());
        overview.put("freeSpaceCount", spaces.stream().filter(space -> "0".equals(space.getStatus())).count());
        overview.put("occupiedSpaceCount", spaces.stream().filter(space -> "1".equals(space.getStatus())).count());
        overview.put("reservedSpaceCount", spaces.stream().filter(space -> "4".equals(space.getStatus())).count());
        overview.put("maintenanceSpaceCount", spaces.stream().filter(space -> "2".equals(space.getStatus())).count());
        overview.put("disabledSpaceCount", spaces.stream().filter(space -> "3".equals(space.getStatus())).count());
        overview.put("activeParkingCount", activeRecords.size());
        overview.put("activeReservationCount", activeReservations.size());
        overview.put("inTodayCount", inTodayCount);
        overview.put("outTodayCount", outTodayCount);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("overview", overview);
        data.put("activeRecords", activeRecordRows);
        data.put("latestRecords", latestRecordRows);
        data.put("spaceRows", spaceRows);
        data.put("serverTime", now);
        return data;
    }
}
