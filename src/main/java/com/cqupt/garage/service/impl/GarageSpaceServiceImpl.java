package com.cqupt.garage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqupt.garage.dto.GarageSpaceDTO;
import com.cqupt.garage.mapper.GarageRecordMapper;
import com.cqupt.garage.mapper.GarageReservationMapper;
import com.cqupt.garage.mapper.GarageSpaceMapper;
import com.cqupt.garage.pojo.GarageRecord;
import com.cqupt.garage.pojo.GarageReservation;
import com.cqupt.garage.pojo.GarageSpace;
import com.cqupt.garage.pojo.User;
import com.cqupt.garage.service.GarageSpaceService;
import com.cqupt.garage.service.UserService;
import com.cqupt.garage.utils.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GarageSpaceServiceImpl extends ServiceImpl<GarageSpaceMapper, GarageSpace> implements GarageSpaceService {

    private static final int DEFAULT_PAGE_SIZE = 6;

    @Autowired
    private UserService userService;

    @Autowired
    private GarageReservationMapper garageReservationMapper;

    @Autowired
    private GarageRecordMapper garageRecordMapper;

    @Override
    public ResultVo<Object> listGarageSpacePage(GarageSpaceDTO dto) {
        QueryWrapper<GarageSpace> queryWrapper = buildPageQuery(dto);
        Page<GarageSpace> page = new Page<>(resolveCurrentPage(dto == null ? null : dto.getPageSize()), DEFAULT_PAGE_SIZE);
        page(page, queryWrapper);
        return ResultVo.ok(page);
    }

    @Override
    public ResultVo<Object> listMyGarageSpacePage(GarageSpaceDTO dto) {
        User currentUser = userService.getCurrentLoginUser();
        QueryWrapper<GarageSpace> queryWrapper = buildPageQuery(dto);

        QueryWrapper<GarageReservation> reservationQuery = new QueryWrapper<>();
        reservationQuery.eq("user_id", currentUser.getId()).eq("reservation_status", "0");
        List<GarageReservation> reservations = garageReservationMapper.selectList(reservationQuery);
        Set<String> reservationSpaceNos = reservations.stream().map(GarageReservation::getSpaceNo).collect(Collectors.toSet());

        QueryWrapper<GarageRecord> recordQuery = new QueryWrapper<>();
        recordQuery.eq("user_id", currentUser.getId()).eq("record_status", "0");
        List<GarageRecord> records = garageRecordMapper.selectList(recordQuery);
        Set<String> occupiedSpaceNos = records.stream().map(GarageRecord::getSpaceNo).collect(Collectors.toSet());

        queryWrapper.and(wrapper -> {
            wrapper.eq("owner_user_id", currentUser.getId());
            if (!reservationSpaceNos.isEmpty()) {
                wrapper.or().in("space_no", reservationSpaceNos);
            }
            if (!occupiedSpaceNos.isEmpty()) {
                wrapper.or().in("space_no", occupiedSpaceNos);
            }
        });

        Page<GarageSpace> page = new Page<>(resolveCurrentPage(dto == null ? null : dto.getPageSize()), DEFAULT_PAGE_SIZE);
        page(page, queryWrapper);
        return ResultVo.ok(page);
    }

    @Override
    public ResultVo<Object> addGarageSpace(GarageSpace garageSpace) {
        User currentUser = userService.getCurrentLoginUser();
        if (!userService.isAdmin(currentUser)) {
            return ResultVo.fail("admin only");
        }
        if (garageSpace == null || isBlank(garageSpace.getAreaName()) || isBlank(garageSpace.getSpaceNo())) {
            return ResultVo.fail("areaName and spaceNo are required");
        }

        QueryWrapper<GarageSpace> duplicateWrapper = new QueryWrapper<>();
        duplicateWrapper.eq("space_no", garageSpace.getSpaceNo().trim());
        if (count(duplicateWrapper) > 0) {
            return ResultVo.fail("spaceNo already exists");
        }

        garageSpace.setAreaName(garageSpace.getAreaName().trim());
        garageSpace.setSpaceNo(garageSpace.getSpaceNo().trim());
        if (isBlank(garageSpace.getSpaceType())) {
            garageSpace.setSpaceType("1");
        }
        if (isBlank(garageSpace.getStatus())) {
            garageSpace.setStatus("0");
        }
        garageSpace.setCreateTime(LocalDateTime.now());
        garageSpace.setUpdateTime(LocalDateTime.now());
        save(garageSpace);
        return ResultVo.ok("space added");
    }

    @Override
    public ResultVo<Object> updateGarageSpace(GarageSpace garageSpace) {
        User currentUser = userService.getCurrentLoginUser();
        if (!userService.isAdmin(currentUser)) {
            return ResultVo.fail("admin only");
        }
        if (garageSpace == null || garageSpace.getId() == null) {
            return ResultVo.fail("space id is required");
        }

        GarageSpace oldSpace = getById(garageSpace.getId());
        if (oldSpace == null) {
            return ResultVo.fail("space not exists");
        }

        if (!isBlank(garageSpace.getSpaceNo())) {
            QueryWrapper<GarageSpace> duplicateWrapper = new QueryWrapper<>();
            duplicateWrapper.eq("space_no", garageSpace.getSpaceNo().trim());
            duplicateWrapper.ne("id", garageSpace.getId());
            if (count(duplicateWrapper) > 0) {
                return ResultVo.fail("spaceNo already exists");
            }
            garageSpace.setSpaceNo(garageSpace.getSpaceNo().trim());
        }

        garageSpace.setUpdateTime(LocalDateTime.now());
        updateById(garageSpace);
        return ResultVo.ok("space updated");
    }

    @Override
    public ResultVo<Object> delGarageSpace(Long id) {
        User currentUser = userService.getCurrentLoginUser();
        if (!userService.isAdmin(currentUser)) {
            return ResultVo.fail("admin only");
        }
        if (id == null) {
            return ResultVo.fail("space id is required");
        }

        GarageSpace garageSpace = getById(id);
        if (garageSpace == null) {
            return ResultVo.fail("space not exists");
        }
        if ("1".equals(garageSpace.getStatus()) || "4".equals(garageSpace.getStatus())) {
            return ResultVo.fail("occupied or reserved space can not be deleted");
        }

        removeById(id);
        return ResultVo.ok("space deleted");
    }

    private QueryWrapper<GarageSpace> buildPageQuery(GarageSpaceDTO dto) {
        QueryWrapper<GarageSpace> queryWrapper = new QueryWrapper<>();
        if (dto != null && !isBlank(dto.getKeyword())) {
            String keyword = dto.getKeyword().trim();
            queryWrapper.and(wrapper -> wrapper.like("space_no", keyword)
                    .or().like("area_name", keyword)
                    .or().like("floor_no", keyword));
        }
        if (dto != null && !isBlank(dto.getStatus())) {
            queryWrapper.eq("status", dto.getStatus().trim());
        }
        queryWrapper.orderByAsc("id");
        return queryWrapper;
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
