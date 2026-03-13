package com.cqupt.garage.service;

import com.cqupt.garage.dto.PlateRecognitionEventDTO;
import com.cqupt.garage.utils.ResultVo;

public interface PlateRecognitionService {

    ResultVo<Object> analyzePlateEvent(PlateRecognitionEventDTO dto);
}
