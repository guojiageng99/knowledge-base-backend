package com.knowledge.base.userauth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.knowledge.base.userauth.entity.User;

public interface UserService extends IService<User> {

    User getByUsername(String username);

    boolean createUser(User user);
}
