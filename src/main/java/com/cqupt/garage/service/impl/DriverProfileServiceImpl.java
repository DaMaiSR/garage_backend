package com.cqupt.garage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqupt.garage.dto.DriverProfileDTO;
import com.cqupt.garage.mapper.DriverProfileMapper;
import com.cqupt.garage.pojo.DriverProfile;
import com.cqupt.garage.pojo.User;
import com.cqupt.garage.service.DriverProfileService;
import com.cqupt.garage.service.UserService;
import com.cqupt.garage.utils.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DriverProfileServiceImpl extends ServiceImpl<DriverProfileMapper, DriverProfile> implements DriverProfileService {

    private static final int DEFAULT_PAGE_SIZE = 6;

    @Autowired
    private UserService userService;

    @Override
    public ResultVo<Object> listDriverProfilePage(DriverProfileDTO dto) {
        User currentUser = userService.getCurrentLoginUser();
        QueryWrapper<DriverProfile> queryWrapper = buildQuery(dto);
        if (!userService.isAdmin(currentUser)) {
            queryWrapper.eq("user_id", currentUser.getId());
        }
        Page<DriverProfile> page = new Page<>(resolveCurrentPage(dto == null ? null : dto.getPageSize()), DEFAULT_PAGE_SIZE);
        page(page, queryWrapper);
        return ResultVo.ok(page);
    }

    @Override
    public ResultVo<Object> addDriverProfile(DriverProfile driverProfile) {
        User currentUser = userService.getCurrentLoginUser();
        if (driverProfile == null || isBlank(driverProfile.getDriverName()) || isBlank(driverProfile.getLicenseNo())) {
            return ResultVo.fail("驾驶员姓名和驾驶证号不能为空");
        }

        QueryWrapper<DriverProfile> duplicateWrapper = new QueryWrapper<>();
        duplicateWrapper.eq("license_no", driverProfile.getLicenseNo().trim());
        if (count(duplicateWrapper) > 0) {
            return ResultVo.fail("驾驶证号已存在");
        }

        driverProfile.setDriverName(driverProfile.getDriverName().trim());
        driverProfile.setLicenseNo(driverProfile.getLicenseNo().trim());
        if (!userService.isAdmin(currentUser) || driverProfile.getUserId() == null) {
            driverProfile.setUserId(currentUser.getId());
        }
        if (isBlank(driverProfile.getLicenseType())) {
            driverProfile.setLicenseType("C1");
        }
        if (isBlank(driverProfile.getStatus())) {
            driverProfile.setStatus("1");
        }
        driverProfile.setCreateTime(LocalDateTime.now());
        driverProfile.setUpdateTime(LocalDateTime.now());
        save(driverProfile);
        return ResultVo.ok("新增驾驶档案成功");
    }

    @Override
    public ResultVo<Object> updateDriverProfile(DriverProfile driverProfile) {
        User currentUser = userService.getCurrentLoginUser();
        if (driverProfile == null || driverProfile.getId() == null) {
            return ResultVo.fail("档案ID不能为空");
        }

        DriverProfile oldProfile = getById(driverProfile.getId());
        if (oldProfile == null) {
            return ResultVo.fail("未找到驾驶档案");
        }
        if (!userService.isAdmin(currentUser) && !currentUser.getId().equals(oldProfile.getUserId())) {
            return ResultVo.fail("无权修改该档案");
        }

        if (!isBlank(driverProfile.getLicenseNo())) {
            QueryWrapper<DriverProfile> duplicateWrapper = new QueryWrapper<>();
            duplicateWrapper.eq("license_no", driverProfile.getLicenseNo().trim());
            duplicateWrapper.ne("id", driverProfile.getId());
            if (count(duplicateWrapper) > 0) {
                return ResultVo.fail("驾驶证号已存在");
            }
            driverProfile.setLicenseNo(driverProfile.getLicenseNo().trim());
        }

        if (!userService.isAdmin(currentUser)) {
            driverProfile.setUserId(oldProfile.getUserId());
        }
        driverProfile.setUpdateTime(LocalDateTime.now());
        updateById(driverProfile);
        return ResultVo.ok("修改驾驶档案成功");
    }

    @Override
    public ResultVo<Object> delDriverProfile(Long id) {
        User currentUser = userService.getCurrentLoginUser();
        if (id == null) {
            return ResultVo.fail("档案ID不能为空");
        }

        DriverProfile oldProfile = getById(id);
        if (oldProfile == null) {
            return ResultVo.fail("未找到驾驶档案");
        }
        if (!userService.isAdmin(currentUser) && !currentUser.getId().equals(oldProfile.getUserId())) {
            return ResultVo.fail("无权删除该档案");
        }

        removeById(id);
        return ResultVo.ok("删除驾驶档案成功");
    }

    private QueryWrapper<DriverProfile> buildQuery(DriverProfileDTO dto) {
        QueryWrapper<DriverProfile> queryWrapper = new QueryWrapper<>();
        if (dto != null && !isBlank(dto.getKeyword())) {
            String keyword = dto.getKeyword().trim();
            queryWrapper.and(wrapper -> wrapper.like("driver_name", keyword)
                    .or().like("license_no", keyword));
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
