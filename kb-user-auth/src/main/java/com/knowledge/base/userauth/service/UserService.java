package com.knowledge.base.userauth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.knowledge.base.userauth.entity.User;
import com.knowledge.base.userauth.vo.LoginVO;
import com.knowledge.base.userauth.vo.UserVO;
import com.knowledge.base.userauth.vo.UserStatisticsVO;
import com.knowledge.base.userauth.dto.UserDTO;
import com.knowledge.base.userauth.dto.UserProfileDTO;
import com.knowledge.base.userauth.dto.RegisterDTO;
import com.knowledge.base.userauth.vo.RegisterVO;
import com.baomidou.mybatisplus.core.metadata.IPage;

public interface UserService extends IService<User> {

    User getByUsername(String username);

    boolean createUser(User user);

    Long createUser(UserDTO userDTO);
    Boolean updateUser(UserDTO userDTO);
    Boolean updateCurrentUserProfile(UserProfileDTO profileDTO);
    Boolean deleteUser(Long userId);
    UserVO getUserById(Long userId);
    IPage<UserVO> pageUsers(Long current, Long size, String keyword, String role, Integer status);
    Boolean resetPassword(Long userId, String newPassword);
    Boolean changePassword(String oldPassword, String newPassword);

    RegisterVO register(RegisterDTO registerDTO);

    String verifyEmail(String token);

    void sendResetCode(String email);

    boolean verifyResetCode(String email, String code);

    void resetPassword(String email, String code, String newPassword);

    LoginVO login(String username, String password);

    void logout(String token);

    LoginVO refresh(String refreshToken);

    UserVO getCurrentUserInfo();
    UserStatisticsVO getUserStatistics(Long userId);
}
