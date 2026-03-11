package com.cqupt.garage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cqupt.garage.dto.GarageSpaceDTO;
import com.cqupt.garage.pojo.GarageSpace;
import com.cqupt.garage.utils.ResultVo;

public interface GarageSpaceService extends IService<GarageSpace> {

    ResultVo<Object> listGarageSpacePage(GarageSpaceDTO dto);

    ResultVo<Object> listMyGarageSpacePage(GarageSpaceDTO dto);

    ResultVo<Object> addGarageSpace(GarageSpace garageSpace);

    ResultVo<Object> updateGarageSpace(GarageSpace garageSpace);

    ResultVo<Object> delGarageSpace(Long id);
}
