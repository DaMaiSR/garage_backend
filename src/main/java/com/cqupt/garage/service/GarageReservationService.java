package com.cqupt.garage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cqupt.garage.dto.GarageReservationDTO;
import com.cqupt.garage.pojo.GarageReservation;
import com.cqupt.garage.utils.ResultVo;

public interface GarageReservationService extends IService<GarageReservation> {

    ResultVo<Object> listGarageReservationPage(GarageReservationDTO dto);

    ResultVo<Object> createReservation(GarageReservation garageReservation);

    ResultVo<Object> cancelReservation(Long id);

    ResultVo<Object> checkInReservation(Long id);
}
