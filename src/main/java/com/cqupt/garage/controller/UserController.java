package com.cqupt.garage.controller;

import com.cqupt.garage.pojo.User;
import com.cqupt.garage.service.UserService;
import com.cqupt.garage.utils.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public ResultVo<User> login(String username, String password) {
        return userService.login(username, password);
    }

    @PostMapping("/login")
    public ResultVo<User> postLogin(String username, String password) {
        return userService.login(username, password);
    }

    @PostMapping("/register")
    public ResultVo<Object> register(User user) {
        return userService.register(user);
    }

    @GetMapping("/logout")
    public ResultVo<Object> logout() {
        return userService.logout();
    }

    @GetMapping("/profileSummary")
    public ResultVo<Object> profileSummary() {
        return userService.getProfileSummary();
    }

    @PostMapping("/updateProfile")
    public ResultVo<Object> updateProfile(User user) {
        return userService.updateProfile(user);
    }
}
