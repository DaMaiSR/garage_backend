package com.cqupt.garage.controller;

import com.cqupt.garage.dto.MarkPaymentPaidDTO;
import com.cqupt.garage.dto.PlateRecognitionEventDTO;
import com.cqupt.garage.service.CheckoutPaymentService;
import com.cqupt.garage.service.PlateRecognitionService;
import com.cqupt.garage.utils.ResultVo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/open/integration")
@CrossOrigin
public class OpenIntegrationController {

    private final PlateRecognitionService plateRecognitionService;
    private final CheckoutPaymentService checkoutPaymentService;

    @Value("${integration.access-key:garage-integration-key}")
    private String integrationAccessKey;

    public OpenIntegrationController(PlateRecognitionService plateRecognitionService,
                                     CheckoutPaymentService checkoutPaymentService) {
        this.plateRecognitionService = plateRecognitionService;
        this.checkoutPaymentService = checkoutPaymentService;
    }

    @PostMapping("/plate/analyze")
    public ResultVo<Object> analyzePlateEvent(PlateRecognitionEventDTO dto,
                                              @RequestHeader(value = "X-Integration-Key", required = false) String accessKey) {
        if (!isValidAccessKey(accessKey)) {
            return ResultVo.fail("集成密钥无效");
        }
        return plateRecognitionService.analyzePlateEvent(dto);
    }

    @PostMapping("/payment/mock/paid")
    public ResultVo<Object> markOrderPaid(MarkPaymentPaidDTO dto,
                                          @RequestHeader(value = "X-Integration-Key", required = false) String accessKey) {
        if (!isValidAccessKey(accessKey)) {
            return ResultVo.fail("集成密钥无效");
        }
        return checkoutPaymentService.markCheckoutOrderPaid(
                dto == null ? null : dto.getOrderNo(),
                dto == null ? null : dto.getTransactionNo(),
                dto == null ? null : dto.getPaidTime(),
                "mock-callback"
        );
    }

    private boolean isValidAccessKey(String accessKey) {
        if (accessKey == null || accessKey.trim().isEmpty()) {
            return false;
        }
        return accessKey.trim().equals(integrationAccessKey);
    }
}
