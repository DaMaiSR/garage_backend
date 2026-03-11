package com.cqupt.garage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqupt.garage.dto.GarageVehicleDTO;
import com.cqupt.garage.mapper.GarageVehicleMapper;
import com.cqupt.garage.pojo.GarageVehicle;
import com.cqupt.garage.pojo.User;
import com.cqupt.garage.service.GarageVehicleService;
import com.cqupt.garage.service.UserService;
import com.cqupt.garage.utils.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GarageVehicleServiceImpl extends ServiceImpl<GarageVehicleMapper, GarageVehicle> implements GarageVehicleService {

    private static final int DEFAULT_PAGE_SIZE = 6;

    @Autowired
    private UserService userService;

    @Override
    public ResultVo<Object> listGarageVehiclePage(GarageVehicleDTO dto) {
        User currentUser = userService.getCurrentLoginUser();
        QueryWrapper<GarageVehicle> queryWrapper = buildQuery(dto);
        if (!userService.isAdmin(currentUser)) {
            queryWrapper.eq("user_id", currentUser.getId());
        }
        Page<GarageVehicle> page = new Page<>(resolveCurrentPage(dto == null ? null : dto.getPageSize()), DEFAULT_PAGE_SIZE);
        page(page, queryWrapper);
        return ResultVo.ok(page);
    }

    @Override
    public ResultVo<Object> listGarageVehicle() {
        User currentUser = userService.getCurrentLoginUser();
        QueryWrapper<GarageVehicle> queryWrapper = new QueryWrapper<>();
        if (!userService.isAdmin(currentUser)) {
            queryWrapper.eq("user_id", currentUser.getId());
        }
        queryWrapper.orderByDesc("id");
        List<GarageVehicle> list = list(queryWrapper);
        return ResultVo.ok(list);
    }

    @Override
    public ResultVo<Object> addGarageVehicle(GarageVehicle garageVehicle) {
        User currentUser = userService.getCurrentLoginUser();
        if (garageVehicle == null || isBlank(garageVehicle.getPlateNo())
                || isBlank(garageVehicle.getOwnerName()) || isBlank(garageVehicle.getOwnerPhone())) {
            return ResultVo.fail("车牌号、车主姓名和电话不能为空");
        }

        QueryWrapper<GarageVehicle> duplicateWrapper = new QueryWrapper<>();
        duplicateWrapper.eq("plate_no", garageVehicle.getPlateNo().trim());
        if (count(duplicateWrapper) > 0) {
            return ResultVo.fail("车牌号已存在");
        }

        garageVehicle.setPlateNo(garageVehicle.getPlateNo().trim());
        garageVehicle.setOwnerName(garageVehicle.getOwnerName().trim());
        garageVehicle.setOwnerPhone(garageVehicle.getOwnerPhone().trim());
        if (!userService.isAdmin(currentUser) || garageVehicle.getUserId() == null) {
            garageVehicle.setUserId(currentUser.getId());
        }
        if (isBlank(garageVehicle.getVehicleType())) {
            garageVehicle.setVehicleType("1");
        }
        if (isBlank(garageVehicle.getMemberType())) {
            garageVehicle.setMemberType("1");
        }
        if (isBlank(garageVehicle.getStatus())) {
            garageVehicle.setStatus("1");
        }
        garageVehicle.setCreateTime(LocalDateTime.now());
        garageVehicle.setUpdateTime(LocalDateTime.now());
        save(garageVehicle);
        return ResultVo.ok("新增车辆成功");
    }

    @Override
    public ResultVo<Object> updateGarageVehicle(GarageVehicle garageVehicle) {
        User currentUser = userService.getCurrentLoginUser();
        if (garageVehicle == null || garageVehicle.getId() == null) {
            return ResultVo.fail("车辆ID不能为空");
        }

        GarageVehicle oldVehicle = getById(garageVehicle.getId());
        if (oldVehicle == null) {
            return ResultVo.fail("未找到车辆");
        }
        if (!userService.isAdmin(currentUser) && !currentUser.getId().equals(oldVehicle.getUserId())) {
            return ResultVo.fail("无权修改该车辆");
        }

        if (!isBlank(garageVehicle.getPlateNo())) {
            QueryWrapper<GarageVehicle> duplicateWrapper = new QueryWrapper<>();
            duplicateWrapper.eq("plate_no", garageVehicle.getPlateNo().trim());
            duplicateWrapper.ne("id", garageVehicle.getId());
            if (count(duplicateWrapper) > 0) {
                return ResultVo.fail("车牌号已存在");
            }
            garageVehicle.setPlateNo(garageVehicle.getPlateNo().trim());
        }

        if (!userService.isAdmin(currentUser)) {
            garageVehicle.setUserId(oldVehicle.getUserId());
        }
        garageVehicle.setUpdateTime(LocalDateTime.now());
        updateById(garageVehicle);
        return ResultVo.ok("修改车辆成功");
    }

    @Override
    public ResultVo<Object> delGarageVehicle(Long id) {
        User currentUser = userService.getCurrentLoginUser();
        if (id == null) {
            return ResultVo.fail("车辆ID不能为空");
        }

        GarageVehicle oldVehicle = getById(id);
        if (oldVehicle == null) {
            return ResultVo.fail("未找到车辆");
        }
        if (!userService.isAdmin(currentUser) && !currentUser.getId().equals(oldVehicle.getUserId())) {
            return ResultVo.fail("无权删除该车辆");
        }

        removeById(id);
        return ResultVo.ok("删除车辆成功");
    }

    private QueryWrapper<GarageVehicle> buildQuery(GarageVehicleDTO dto) {
        QueryWrapper<GarageVehicle> queryWrapper = new QueryWrapper<>();
        if (dto != null && !isBlank(dto.getKeyword())) {
            String keyword = dto.getKeyword().trim();
            queryWrapper.and(wrapper -> wrapper.like("plate_no", keyword)
                    .or().like("owner_name", keyword));
        }
        if (dto != null && !isBlank(dto.getStatus())) {
            queryWrapper.eq("status", dto.getStatus().trim());
        }
        queryWrapper.orderByDesc("id");
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
