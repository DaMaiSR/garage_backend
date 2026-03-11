package com.cqupt.garage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cqupt.garage.pojo.User;
import com.cqupt.garage.utils.ResultVo;

public interface UserService extends IService<User> {

    ResultVo<User> login(String username, String password);

    ResultVo<Object> register(User user);

    ResultVo<Object> logout();

    ResultVo<Object> getProfileSummary();

    ResultVo<Object> updateProfile(User user);

    User getCurrentLoginUser();

    boolean isAdmin(User user);
}
