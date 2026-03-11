package com.cqupt.garage.controller;

import com.cqupt.garage.service.GarageMonitorService;
import com.cqupt.garage.utils.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/garageMonitor")
@CrossOrigin
public class GarageMonitorController {

    @Autowired
    private GarageMonitorService garageMonitorService;

    @GetMapping("/realtime")
    public ResultVo<Object> getRealtimeData() {
        return garageMonitorService.getRealtimeData();
    }
}
