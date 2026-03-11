package com.cqupt.garage.websocket;

import com.cqupt.garage.service.GarageMonitorService;
import com.cqupt.garage.utils.ResultVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RealtimeMonitorPushTask {

    @Autowired
    private GarageMonitorService garageMonitorService;

    @Autowired
    private RealtimeWebSocketHandler realtimeWebSocketHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${monitor.push-interval-ms:5000}")
    public void pushRealtimeData() {
        try {
            String payload = objectMapper.writeValueAsString(ResultVo.ok(garageMonitorService.buildRealtimeSnapshot()));
            realtimeWebSocketHandler.broadcast(payload);
        } catch (Exception ignored) {
        }
    }
}
