package com.cqupt.garage.controller;

import com.cqupt.garage.dto.GarageReservationDTO;
import com.cqupt.garage.pojo.GarageReservation;
import com.cqupt.garage.service.GarageReservationService;
import com.cqupt.garage.utils.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/garageReservation")
@CrossOrigin
public class GarageReservationController {

    @Autowired
    private GarageReservationService garageReservationService;

    @GetMapping("/listGarageReservationPage")
    public ResultVo<Object> listGarageReservationPage(GarageReservationDTO dto) {
        return garageReservationService.listGarageReservationPage(dto);
    }

    @PostMapping("/createReservation")
    public ResultVo<Object> createReservation(GarageReservation garageReservation) {
        return garageReservationService.createReservation(garageReservation);
    }

    @PostMapping("/cancelReservation")
    public ResultVo<Object> cancelReservation(Long id) {
        return garageReservationService.cancelReservation(id);
    }

    @PostMapping("/checkInReservation")
    public ResultVo<Object> checkInReservation(Long id) {
        return garageReservationService.checkInReservation(id);
    }
}
