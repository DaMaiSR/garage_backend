package com.cqupt.garage.controller;

import com.cqupt.garage.dto.DriverProfileDTO;
import com.cqupt.garage.pojo.DriverProfile;
import com.cqupt.garage.service.DriverProfileService;
import com.cqupt.garage.utils.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/driverProfile")
@CrossOrigin
public class DriverProfileController {

    @Autowired
    private DriverProfileService driverProfileService;

    @GetMapping("/listDriverProfilePage")
    public ResultVo<Object> listDriverProfilePage(DriverProfileDTO dto) {
        return driverProfileService.listDriverProfilePage(dto);
    }

    @PostMapping("/addDriverProfile")
    public ResultVo<Object> addDriverProfile(DriverProfile driverProfile) {
        return driverProfileService.addDriverProfile(driverProfile);
    }

    @PostMapping("/updateDriverProfile")
    public ResultVo<Object> updateDriverProfile(DriverProfile driverProfile) {
        return driverProfileService.updateDriverProfile(driverProfile);
    }

    @GetMapping("/delDriverProfile")
    public ResultVo<Object> delDriverProfile(Long id) {
        return driverProfileService.delDriverProfile(id);
    }
}
