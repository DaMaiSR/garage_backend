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

        String licenseNo = normalizeLicenseNo(driverProfile.getLicenseNo());
        String phone = isBlank(driverProfile.getPhone()) ? "" : driverProfile.getPhone().trim();
        if (!phone.isEmpty() && !phone.matches("^1\\d{10}$")) {
            return ResultVo.fail("手机号格式不正确");
        }

        QueryWrapper<DriverProfile> duplicateWrapper = new QueryWrapper<>();
        duplicateWrapper.eq("license_no", licenseNo);
        if (count(duplicateWrapper) > 0) {
            return ResultVo.fail("驾驶证号已存在");
        }

        driverProfile.setDriverName(driverProfile.getDriverName().trim());
        driverProfile.setLicenseNo(licenseNo);
        driverProfile.setPhone(phone);
        if (!userService.isAdmin(currentUser) || driverProfile.getUserId() == null) {
            driverProfile.setUserId(currentUser.getId());
        }
        if (isBlank(driverProfile.getLicenseType())) {
            driverProfile.setLicenseType("C1");
        } else {
            driverProfile.setLicenseType(driverProfile.getLicenseType().trim().toUpperCase());
        }
        if (isBlank(driverProfile.getStatus())) {
            driverProfile.setStatus("1");
        }
        if (!isBlank(driverProfile.getRemark()) && driverProfile.getRemark().trim().length() > 100) {
            return ResultVo.fail("备注长度不能超过100");
        }
        driverProfile.setRemark(isBlank(driverProfile.getRemark()) ? null : driverProfile.getRemark().trim());
        driverProfile.setCreateTime(LocalDateTime.now());
        driverProfile.setUpdateTime(LocalDateTime.now());
        save(driverProfile);
        return ResultVo.ok("驾驶档案新增成功");
    }

    @Override
    public ResultVo<Object> updateDriverProfile(DriverProfile driverProfile) {
        User currentUser = userService.getCurrentLoginUser();
        if (driverProfile == null || driverProfile.getId() == null) {
            return ResultVo.fail("档案ID不能为空");
        }

        DriverProfile oldProfile = getById(driverProfile.getId());
        if (oldProfile == null) {
            return ResultVo.fail("驾驶档案不存在");
        }
        if (!userService.isAdmin(currentUser) && !currentUser.getId().equals(oldProfile.getUserId())) {
            return ResultVo.fail("无权限操作");
        }

        if (!isBlank(driverProfile.getLicenseNo())) {
            String licenseNo = normalizeLicenseNo(driverProfile.getLicenseNo());
            QueryWrapper<DriverProfile> duplicateWrapper = new QueryWrapper<>();
            duplicateWrapper.eq("license_no", licenseNo);
            duplicateWrapper.ne("id", driverProfile.getId());
            if (count(duplicateWrapper) > 0) {
                return ResultVo.fail("驾驶证号已存在");
            }
            driverProfile.setLicenseNo(licenseNo);
        }

        if (!isBlank(driverProfile.getPhone())) {
            String phone = driverProfile.getPhone().trim();
            if (!phone.matches("^1\\d{10}$")) {
                return ResultVo.fail("手机号格式不正确");
            }
            driverProfile.setPhone(phone);
        }
        if (!isBlank(driverProfile.getLicenseType())) {
            driverProfile.setLicenseType(driverProfile.getLicenseType().trim().toUpperCase());
        }
        if (!isBlank(driverProfile.getRemark()) && driverProfile.getRemark().trim().length() > 100) {
            return ResultVo.fail("备注长度不能超过100");
        }
        if (!isBlank(driverProfile.getRemark())) {
            driverProfile.setRemark(driverProfile.getRemark().trim());
        }

        if (!userService.isAdmin(currentUser)) {
            driverProfile.setUserId(oldProfile.getUserId());
        }
        driverProfile.setUpdateTime(LocalDateTime.now());
        updateById(driverProfile);
        return ResultVo.ok("驾驶档案更新成功");
    }

    @Override
    public ResultVo<Object> delDriverProfile(Long id) {
        User currentUser = userService.getCurrentLoginUser();
        if (id == null) {
            return ResultVo.fail("档案ID不能为空");
        }

        DriverProfile oldProfile = getById(id);
        if (oldProfile == null) {
            return ResultVo.fail("驾驶档案不存在");
        }
        if (!userService.isAdmin(currentUser) && !currentUser.getId().equals(oldProfile.getUserId())) {
            return ResultVo.fail("无权限操作");
        }

        removeById(id);
        return ResultVo.ok("驾驶档案删除成功");
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

    private String normalizeLicenseNo(String licenseNo) {
        if (isBlank(licenseNo)) {
            return "";
        }
        return licenseNo.trim().toUpperCase();
    }
}
