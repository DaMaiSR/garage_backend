package com.cqupt.garage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cqupt.garage.dto.DriverProfileDTO;
import com.cqupt.garage.pojo.DriverProfile;
import com.cqupt.garage.utils.ResultVo;

public interface DriverProfileService extends IService<DriverProfile> {

    ResultVo<Object> listDriverProfilePage(DriverProfileDTO dto);

    ResultVo<Object> addDriverProfile(DriverProfile driverProfile);

    ResultVo<Object> updateDriverProfile(DriverProfile driverProfile);

    ResultVo<Object> delDriverProfile(Long id);
}
