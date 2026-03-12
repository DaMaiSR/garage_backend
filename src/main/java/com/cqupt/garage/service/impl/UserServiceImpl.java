package com.cqupt.garage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqupt.garage.mapper.DriverProfileMapper;
import com.cqupt.garage.mapper.GarageRecordMapper;
import com.cqupt.garage.mapper.GarageReservationMapper;
import com.cqupt.garage.mapper.GarageVehicleMapper;
import com.cqupt.garage.mapper.UserMapper;
import com.cqupt.garage.pojo.DriverProfile;
import com.cqupt.garage.pojo.GarageRecord;
import com.cqupt.garage.pojo.GarageReservation;
import com.cqupt.garage.pojo.GarageVehicle;
import com.cqupt.garage.pojo.MenuChild;
import com.cqupt.garage.pojo.MenuItem;
import com.cqupt.garage.pojo.User;
import com.cqupt.garage.service.UserService;
import com.cqupt.garage.utils.JwtUtils;
import com.cqupt.garage.utils.ResultVo;
import com.cqupt.garage.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private GarageVehicleMapper garageVehicleMapper;

    @Autowired
    private DriverProfileMapper driverProfileMapper;

    @Autowired
    private GarageRecordMapper garageRecordMapper;

    @Autowired
    private GarageReservationMapper garageReservationMapper;

    @Override
    public ResultVo<User> login(String username, String password) {
        if (isBlank(username) || isBlank(password)) {
            return ResultVo.fail("用户名或密码不能为空");
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username.trim());
        queryWrapper.eq("is_deleted", 0);
        User user = getOne(queryWrapper, false);
        if (user == null) {
            return ResultVo.fail("用户名或密码错误");
        }
        if (!matchesPassword(password.trim(), user.getPassword())) {
            return ResultVo.fail("用户名或密码错误");
        }

        if (!isEncodedPassword(user.getPassword())) {
            User updateUser = new User();
            updateUser.setId(user.getId());
            updateUser.setPassword(passwordEncoder.encode(password.trim()));
            updateUser.setUpdateTime(LocalDateTime.now());
            updateById(updateUser);
            user.setPassword(updateUser.getPassword());
        }

        String token = jwtUtils.createToken(user.getId(), user.getUsername(), user.getRole());
        user = sanitizeUser(user);
        user.setMenuList(buildMenus(user.getRole()));
        return ResultVo.ok(user, token);
    }

    @Override
    public ResultVo<Object> register(User user) {
        if (user == null || isBlank(user.getUsername()) || isBlank(user.getPassword())) {
            return ResultVo.fail("用户名或密码不能为空");
        }

        String username = user.getUsername().trim();
        if (username.length() < 3) {
            return ResultVo.fail("用户名长度不能少于3位");
        }
        if (user.getPassword().trim().length() < 6) {
            return ResultVo.fail("密码长度不能少于6位");
        }
        if (!isBlank(user.getPhone()) && !user.getPhone().trim().matches("^1\\d{10}$")) {
            return ResultVo.fail("手机号格式不正确");
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("is_deleted", 0);
        if (count(queryWrapper) > 0) {
            return ResultVo.fail("用户名已存在");
        }

        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(user.getPassword().trim()));
        user.setRole("user");
        user.setDisplayName(isBlank(user.getDisplayName()) ? username : user.getDisplayName().trim());
        user.setPhone(isBlank(user.getPhone()) ? null : user.getPhone().trim());
        user.setLicenseNo(isBlank(user.getLicenseNo()) ? null : user.getLicenseNo().trim().toUpperCase());
        user.setLicenseType(isBlank(user.getLicenseType()) ? null : user.getLicenseType().trim().toUpperCase());
        user.setIsDeleted(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        save(user);
        return ResultVo.ok("注册成功");
    }

    @Override
    public ResultVo<Object> logout() {
        return ResultVo.ok("退出成功");
    }

    @Override
    public ResultVo<Object> getProfileSummary() {
        User currentUser = getCurrentLoginUser();

        QueryWrapper<GarageVehicle> vehicleWrapper = new QueryWrapper<>();
        vehicleWrapper.eq("user_id", currentUser.getId()).orderByDesc("id");
        List<GarageVehicle> vehicles = garageVehicleMapper.selectList(vehicleWrapper);

        QueryWrapper<DriverProfile> profileWrapper = new QueryWrapper<>();
        profileWrapper.eq("user_id", currentUser.getId()).orderByDesc("id");
        List<DriverProfile> profiles = driverProfileMapper.selectList(profileWrapper);

        QueryWrapper<GarageReservation> reservationWrapper = new QueryWrapper<>();
        reservationWrapper.eq("user_id", currentUser.getId()).orderByDesc("id").last("limit 10");
        List<GarageReservation> reservations = garageReservationMapper.selectList(reservationWrapper);

        QueryWrapper<GarageRecord> recordWrapper = new QueryWrapper<>();
        recordWrapper.eq("user_id", currentUser.getId()).orderByDesc("id").last("limit 10");
        List<GarageRecord> records = garageRecordMapper.selectList(recordWrapper);

        long activeReservationCount = reservations.stream().filter(item -> "0".equals(item.getReservationStatus())).count();
        long activeParkingCount = records.stream().filter(item -> "0".equals(item.getRecordStatus())).count();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("user", sanitizeUser(currentUser));
        summary.put("vehicles", vehicles);
        summary.put("driverProfiles", profiles);
        summary.put("reservations", reservations);
        summary.put("parkingRecords", records);
        summary.put("vehicleCount", vehicles.size());
        summary.put("driverProfileCount", profiles.size());
        summary.put("activeReservationCount", activeReservationCount);
        summary.put("activeParkingCount", activeParkingCount);
        return ResultVo.ok(summary);
    }

    @Override
    public ResultVo<Object> updateProfile(User user) {
        User currentUser = getCurrentLoginUser();
        if (user == null) {
            return ResultVo.fail("请求参数不能为空");
        }

        String phone = user.getPhone() == null ? null : user.getPhone().trim();
        if (phone != null && !phone.isEmpty() && !phone.matches("^1\\d{10}$")) {
            return ResultVo.fail("手机号格式不正确");
        }
        if (!isBlank(user.getPassword()) && user.getPassword().trim().length() < 6) {
            return ResultVo.fail("密码长度不能少于6位");
        }

        User updateUser = new User();
        updateUser.setId(currentUser.getId());
        if (user.getDisplayName() != null) {
            String displayName = user.getDisplayName().trim();
            updateUser.setDisplayName(displayName.isEmpty() ? currentUser.getUsername() : displayName);
        }
        if (phone != null) {
            updateUser.setPhone(phone);
        }
        if (user.getLicenseNo() != null) {
            String licenseNo = user.getLicenseNo().trim();
            updateUser.setLicenseNo(licenseNo.isEmpty() ? "" : licenseNo.toUpperCase());
        }
        if (user.getLicenseType() != null) {
            String licenseType = user.getLicenseType().trim();
            updateUser.setLicenseType(licenseType.isEmpty() ? "" : licenseType.toUpperCase());
        }
        if (!isBlank(user.getPassword())) {
            updateUser.setPassword(passwordEncoder.encode(user.getPassword().trim()));
        }
        updateUser.setUpdateTime(LocalDateTime.now());
        updateById(updateUser);

        User dbUser = getById(currentUser.getId());
        return ResultVo.ok(sanitizeUser(dbUser), "更新成功");
    }

    @Override
    public User getCurrentLoginUser() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("登录状态无效");
        }
        User user = getById(userId);
        if (user == null || (user.getIsDeleted() != null && user.getIsDeleted() != 0)) {
            throw new RuntimeException("用户不存在");
        }
        return user;
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && "admin".equals(user.getRole());
    }

    private List<MenuItem> buildMenus(String role) {
        if ("admin".equals(role)) {
            return buildAdminMenus();
        }
        return buildUserMenus();
    }

    private List<MenuItem> buildAdminMenus() {
        List<MenuItem> menus = new ArrayList<>();
        menus.add(menu("1", "车库管理", "Van",
                child("车位管理", "/garage/spaceManage", "SetUp"),
                child("预约管理", "/garage/reservationManage", "Calendar"),
                child("实时监控", "/garage/realtimeMonitor", "Monitor")));
        menus.add(menu("2", "档案管理", "User",
                child("驾驶档案管理", "/garage/driverProfile", "Tickets"),
                child("车辆信息管理", "/garage/vehicleManage", "Management")));
        return menus;
    }

    private List<MenuItem> buildUserMenus() {
        List<MenuItem> menus = new ArrayList<>();
        menus.add(menu("1", "车库服务", "Van",
                child("车位查询", "/garage/spaceManage", "SetUp"),
                child("停车预约", "/garage/reservationManage", "Calendar"),
                child("我的车位", "/garage/mySpace", "House"),
                child("停车出库", "/garage/parkingRecord", "List")));
        menus.add(menu("2", "我的档案", "User",
                child("驾驶档案管理", "/garage/driverProfile", "Tickets"),
                child("车辆信息管理", "/garage/vehicleManage", "Management"),
                child("个人中心", "/garage/profileCenter", "Avatar")));
        return menus;
    }

    private MenuItem menu(String index, String title, String icon, MenuChild... children) {
        MenuItem item = new MenuItem();
        item.setMenusIndex(index);
        item.setTitle(title);
        item.setIcon(icon);
        item.setChildren(Arrays.asList(children));
        return item;
    }

    private MenuChild child(String title, String path, String icon) {
        MenuChild child = new MenuChild();
        child.setTitle(title);
        child.setPath(path);
        child.setIcon(icon);
        return child;
    }

    private User sanitizeUser(User user) {
        if (user == null) {
            return null;
        }
        user.setPassword(null);
        return user;
    }

    private boolean matchesPassword(String rawPassword, String dbPassword) {
        if (isBlank(dbPassword)) {
            return false;
        }
        if (isEncodedPassword(dbPassword)) {
            return passwordEncoder.matches(rawPassword, dbPassword);
        }
        return rawPassword.equals(dbPassword);
    }

    private boolean isEncodedPassword(String value) {
        if (isBlank(value)) {
            return false;
        }
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
