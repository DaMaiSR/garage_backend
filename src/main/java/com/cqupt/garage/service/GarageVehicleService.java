package com.cqupt.garage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cqupt.garage.dto.GarageVehicleDTO;
import com.cqupt.garage.pojo.GarageVehicle;
import com.cqupt.garage.utils.ResultVo;

public interface GarageVehicleService extends IService<GarageVehicle> {

    ResultVo<Object> listGarageVehiclePage(GarageVehicleDTO dto);

    ResultVo<Object> listGarageVehicle();

    ResultVo<Object> addGarageVehicle(GarageVehicle garageVehicle);

    ResultVo<Object> updateGarageVehicle(GarageVehicle garageVehicle);

    ResultVo<Object> delGarageVehicle(Long id);
}
