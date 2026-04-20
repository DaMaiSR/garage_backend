package com.cqupt.garage.controller;

import com.cqupt.garage.service.FeeRuleService;
import com.cqupt.garage.utils.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping({"/feeRule", "/api/admin/fee-rules"})
@CrossOrigin
public class FeeRuleController {

    @Autowired
    private FeeRuleService feeRuleService;

    @GetMapping({"/getCurrent", "/current"})
    public ResultVo<Object> getCurrent() {
        return feeRuleService.getCurrentFeeRule();
    }

    @PostMapping("/updateCurrent")
    public ResultVo<Object> updateCurrent(Integer freeMinutes, String hourlyRate, String capAmount, String remark) {
        return feeRuleService.updateCurrentFeeRule(freeMinutes, hourlyRate, capAmount, remark);
    }

    @PutMapping("/current")
    public ResultVo<Object> updateCurrentByJson(@RequestBody(required = false) Map<String, Object> body) {
        if (body == null) {
            return feeRuleService.updateCurrentFeeRule(null, null, null, null);
        }
        Integer freeMinutes = null;
        if (body.get("freeMinutes") != null) {
            try {
                freeMinutes = Integer.valueOf(String.valueOf(body.get("freeMinutes")));
            } catch (Exception ex) {
                return ResultVo.fail("免费时长格式不正确");
            }
        }
        String hourlyRate = body.get("hourlyRate") == null ? null : String.valueOf(body.get("hourlyRate"));
        String capAmount = body.get("capAmount") == null ? null : String.valueOf(body.get("capAmount"));
        String remark = body.get("remark") == null ? null : String.valueOf(body.get("remark"));
        return feeRuleService.updateCurrentFeeRule(freeMinutes, hourlyRate, capAmount, remark);
    }
}

