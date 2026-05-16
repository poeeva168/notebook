package com.notebook.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.notebook.dto.LoginResponse;
import com.notebook.dto.RegisterRequest;
import com.notebook.dto.UserVO;
import com.notebook.entity.User;

public interface UserService extends IService<User> {

    void register(RegisterRequest request);

    LoginResponse login(String username, String password);

    UserVO getUserInfo(Long userId);

    User getUserById(Long userId);
}
