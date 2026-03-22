package com.cqupt.garage.service;

import com.cqupt.garage.utils.ResultVo;
import org.springframework.web.multipart.MultipartFile;

public interface PlateRecognitionWorkflowService {

    ResultVo<Object> recognizeAndTransit(MultipartFile imageFile, String action, String cameraCode, String spaceNo);
}
