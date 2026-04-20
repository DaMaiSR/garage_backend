package com.cqupt.garage.service;

import com.cqupt.garage.utils.ResultVo;

public interface FeeRuleService {

    ResultVo<Object> getCurrentFeeRule();

    ResultVo<Object> updateCurrentFeeRule(Integer freeMinutes, String hourlyRate, String capAmount, String remark);

    String calcFeeByMinutes(long minutes);
}

