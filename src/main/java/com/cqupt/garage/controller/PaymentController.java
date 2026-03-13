package com.cqupt.garage.controller;

import com.cqupt.garage.dto.CompleteCheckoutDTO;
import com.cqupt.garage.dto.CreateCheckoutOrderDTO;
import com.cqupt.garage.pojo.User;
import com.cqupt.garage.service.CheckoutPaymentService;
import com.cqupt.garage.service.UserService;
import com.cqupt.garage.utils.ResultVo;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment")
@CrossOrigin
public class PaymentController {

    private final CheckoutPaymentService checkoutPaymentService;
    private final UserService userService;

    public PaymentController(CheckoutPaymentService checkoutPaymentService, UserService userService) {
        this.checkoutPaymentService = checkoutPaymentService;
        this.userService = userService;
    }

    @PostMapping("/createCheckoutOrder")
    public ResultVo<Object> createCheckoutOrder(CreateCheckoutOrderDTO dto) {
        User currentUser = userService.getCurrentLoginUser();
        return checkoutPaymentService.createCheckoutOrder(
                dto == null ? null : dto.getRecordId(),
                dto == null ? null : dto.getPayMethod(),
                currentUser.getId(),
                userService.isAdmin(currentUser),
                "USER_APP"
        );
    }

    @GetMapping("/queryCheckoutOrder")
    public ResultVo<Object> queryCheckoutOrder(String orderNo) {
        User currentUser = userService.getCurrentLoginUser();
        return checkoutPaymentService.queryCheckoutOrder(
                orderNo,
                currentUser.getId(),
                userService.isAdmin(currentUser)
        );
    }

    @PostMapping("/completeCheckout")
    public ResultVo<Object> completeCheckout(CompleteCheckoutDTO dto) {
        User currentUser = userService.getCurrentLoginUser();
        return checkoutPaymentService.completeCheckout(
                dto == null ? null : dto.getOrderNo(),
                dto == null ? null : dto.getOutTime(),
                currentUser.getId(),
                userService.isAdmin(currentUser)
        );
    }
}
