package com.cqupt.garage.service;

import com.cqupt.garage.utils.ResultVo;

import java.util.Map;

public interface GarageMonitorService {

    ResultVo<Object> getRealtimeData();

    Map<String, Object> buildRealtimeSnapshot();
}
