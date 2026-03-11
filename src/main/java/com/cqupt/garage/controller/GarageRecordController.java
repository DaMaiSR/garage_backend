package com.cqupt.garage.controller;

import com.cqupt.garage.dto.GarageRecordDTO;
import com.cqupt.garage.pojo.GarageRecord;
import com.cqupt.garage.service.GarageRecordService;
import com.cqupt.garage.utils.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/garageRecord")
@CrossOrigin
public class GarageRecordController {

    @Autowired
    private GarageRecordService garageRecordService;

    @GetMapping("/listGarageRecordPage")
    public ResultVo<Object> listGarageRecordPage(GarageRecordDTO dto) {
        return garageRecordService.listGarageRecordPage(dto);
    }

    @PostMapping("/addGarageInRecord")
    public ResultVo<Object> addGarageInRecord(GarageRecord garageRecord) {
        return garageRecordService.addGarageInRecord(garageRecord);
    }

    @PostMapping("/updateGarageOutRecord")
    public ResultVo<Object> updateGarageOutRecord(GarageRecord garageRecord) {
        return garageRecordService.updateGarageOutRecord(garageRecord);
    }
}
