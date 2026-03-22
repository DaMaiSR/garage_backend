package com.cqupt.garage.controller;

import com.cqupt.garage.service.PlateRecognitionWorkflowService;
import com.cqupt.garage.utils.ResultVo;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/plateRecognition")
@CrossOrigin
public class PlateRecognitionController {

    private final PlateRecognitionWorkflowService plateRecognitionWorkflowService;

    public PlateRecognitionController(PlateRecognitionWorkflowService plateRecognitionWorkflowService) {
        this.plateRecognitionWorkflowService = plateRecognitionWorkflowService;
    }

    @PostMapping("/recognizeAndTransit")
    public ResultVo<Object> recognizeAndTransit(@RequestParam("imageFile") MultipartFile imageFile,
                                                @RequestParam(value = "action", required = false) String action,
                                                @RequestParam(value = "cameraCode", required = false) String cameraCode,
                                                @RequestParam(value = "spaceNo", required = false) String spaceNo) {
        return plateRecognitionWorkflowService.recognizeAndTransit(imageFile, action, cameraCode, spaceNo);
    }
}
