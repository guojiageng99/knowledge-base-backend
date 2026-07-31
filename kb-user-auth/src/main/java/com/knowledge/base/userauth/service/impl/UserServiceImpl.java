package com.knowledge.base.userauth.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.knowledge.base.common.exception.BusinessException;
import com.knowledge.base.common.result.ErrorCode;
import com.knowledge.base.common.result.ResultCode;
import com.knowledge.base.common.utils.JwtTokenUtil;
import com.knowledge.base.common.utils.UserContextUtil;
import com.knowledge.base.userauth.entity.User;
import com.knowledge.base.userauth.mapper.UserMapper;
import com.knowledge.base.userauth.service.UserService;
import com.knowledge.base.userauth.vo.LoginVO;
import com.knowledge.base.userauth.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    public User getByUsername(String username) {
        return baseMapper.selectByUsername(username);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createUser(User user) {
        if (getByUsername(user.getUsername()) != null) throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getStatus() == null) user.setStatus(1);
        return save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO login(String username, String password) {
        User user = getByUsername(username);
        if (user == null) throw new BusinessException(ResultCode.USER_NOT_EXIST);
        if (!Integer.valueOf(1).equals(user.getStatus())) throw new BusinessException(ResultCode.USER_DISABLED);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }
        user.setLastLoginTime(LocalDateTime.now());
        updateById(user);
        return buildLoginVO(user);
    }

    @Override
    public void logout(String token) {
        // Stateless JWT logout is client-side; a Redis blacklist is added in the next iteration.
    }

    @Override
    public LoginVO refresh(String refreshToken) {
        Long userId = jwtTokenUtil.getUserIdFromToken(refreshToken);
        if (userId == null || !jwtTokenUtil.isRefreshToken(refreshToken)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        User user = getById(userId);
        if (user == null || !Integer.valueOf(1).equals(user.getStatus())) throw new BusinessException(ResultCode.USER_DISABLED);
        return LoginVO.builder()
                .accessToken(jwtTokenUtil.refreshToken(refreshToken))
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(7200L)
                .userInfo(toLoginUserInfo(user))
                .build();
    }

    @Override
    public UserVO getCurrentUserInfo() {
        Long userId = UserContextUtil.getUserId();
        if (userId == null) throw new BusinessException(ResultCode.UNAUTHORIZED);
        User user = getById(userId);
        if (user == null || !Integer.valueOf(1).equals(user.getStatus())) throw new BusinessException(ResultCode.UNAUTHORIZED);
        return toUserVO(user);
    }

    private LoginVO buildLoginVO(User user) {
        return LoginVO.builder()
                .accessToken(jwtTokenUtil.generateAccessToken(user.getId()))
                .refreshToken(jwtTokenUtil.generateRefreshToken(user.getId()))
                .tokenType("Bearer")
                .expiresIn(7200L)
                .userInfo(toLoginUserInfo(user))
                .build();
    }

    private LoginVO.UserInfo toLoginUserInfo(User user) {
        return LoginVO.UserInfo.builder().userId(user.getId()).username(user.getUsername())
                .nickname(user.getRealName()).email(user.getEmail()).phone(user.getPhone()).avatar(user.getAvatar()).build();
    }

    private UserVO toUserVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId()); vo.setUsername(user.getUsername()); vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone()); vo.setAvatar(user.getAvatar()); vo.setRealName(user.getRealName());
        vo.setDepartment(user.getDepartment()); vo.setPosition(user.getPosition()); vo.setStatus(user.getStatus());
        return vo;
    }
}
