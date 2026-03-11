package com.cqupt.garage.controller;

import com.cqupt.garage.dto.GarageVehicleDTO;
import com.cqupt.garage.pojo.GarageVehicle;
import com.cqupt.garage.service.GarageVehicleService;
import com.cqupt.garage.utils.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/garageVehicle")
@CrossOrigin
public class GarageVehicleController {

    @Autowired
    private GarageVehicleService garageVehicleService;

    @GetMapping("/listGarageVehiclePage")
    public ResultVo<Object> listGarageVehiclePage(GarageVehicleDTO dto) {
        return garageVehicleService.listGarageVehiclePage(dto);
    }

    @GetMapping("/listGarageVehicle")
    public ResultVo<Object> listGarageVehicle() {
        return garageVehicleService.listGarageVehicle();
    }

    @PostMapping("/addGarageVehicle")
    public ResultVo<Object> addGarageVehicle(GarageVehicle garageVehicle) {
        return garageVehicleService.addGarageVehicle(garageVehicle);
    }

    @PostMapping("/updateGarageVehicle")
    public ResultVo<Object> updateGarageVehicle(GarageVehicle garageVehicle) {
        return garageVehicleService.updateGarageVehicle(garageVehicle);
    }

    @GetMapping("/delGarageVehicle")
    public ResultVo<Object> delGarageVehicle(Long id) {
        return garageVehicleService.delGarageVehicle(id);
    }
}
