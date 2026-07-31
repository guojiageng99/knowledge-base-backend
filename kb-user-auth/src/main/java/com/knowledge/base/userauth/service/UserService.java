package com.knowledge.base.userauth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.knowledge.base.userauth.entity.User;
import com.knowledge.base.userauth.vo.LoginVO;
import com.knowledge.base.userauth.vo.UserVO;

public interface UserService extends IService<User> {

    User getByUsername(String username);

    boolean createUser(User user);

    LoginVO login(String username, String password);

    void logout(String token);

    LoginVO refresh(String refreshToken);

    UserVO getCurrentUserInfo();
}
