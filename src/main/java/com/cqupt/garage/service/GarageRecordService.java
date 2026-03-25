package com.cqupt.garage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cqupt.garage.dto.GarageRecordDTO;
import com.cqupt.garage.pojo.GarageRecord;
import com.cqupt.garage.utils.ResultVo;

public interface GarageRecordService extends IService<GarageRecord> {

    ResultVo<Object> listGarageRecordPage(GarageRecordDTO dto);

    ResultVo<Object> addGarageInRecord(GarageRecord garageRecord);

    ResultVo<Object> payGarageOutRecord(GarageRecord garageRecord);

    ResultVo<Object> updateGarageOutRecord(GarageRecord garageRecord);
}
