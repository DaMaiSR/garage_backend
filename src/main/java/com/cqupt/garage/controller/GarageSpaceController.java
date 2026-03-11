package com.cqupt.garage.controller;

import com.cqupt.garage.dto.GarageSpaceDTO;
import com.cqupt.garage.pojo.GarageSpace;
import com.cqupt.garage.service.GarageSpaceService;
import com.cqupt.garage.utils.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/garageSpace")
@CrossOrigin
public class GarageSpaceController {

    @Autowired
    private GarageSpaceService garageSpaceService;

    @GetMapping("/listGarageSpacePage")
    public ResultVo<Object> listGarageSpacePage(GarageSpaceDTO dto) {
        return garageSpaceService.listGarageSpacePage(dto);
    }

    @GetMapping("/listMyGarageSpacePage")
    public ResultVo<Object> listMyGarageSpacePage(GarageSpaceDTO dto) {
        return garageSpaceService.listMyGarageSpacePage(dto);
    }

    @PostMapping("/addGarageSpace")
    public ResultVo<Object> addGarageSpace(GarageSpace garageSpace) {
        return garageSpaceService.addGarageSpace(garageSpace);
    }

    @PostMapping("/updateGarageSpace")
    public ResultVo<Object> updateGarageSpace(GarageSpace garageSpace) {
        return garageSpaceService.updateGarageSpace(garageSpace);
    }

    @GetMapping("/delGarageSpace")
    public ResultVo<Object> delGarageSpace(Long id) {
        return garageSpaceService.delGarageSpace(id);
    }
}
