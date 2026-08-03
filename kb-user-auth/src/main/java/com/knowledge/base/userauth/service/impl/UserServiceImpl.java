package com.knowledge.base.userauth.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.knowledge.base.common.exception.BusinessException;
import com.knowledge.base.common.result.ErrorCode;
import com.knowledge.base.common.result.ResultCode;
import com.knowledge.base.common.utils.JwtTokenUtil;
import com.knowledge.base.common.utils.UserContextUtil;
import com.knowledge.base.userauth.entity.User;
import com.knowledge.base.userauth.dto.UserDTO;
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
    public Long createUser(UserDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername()); user.setPassword(dto.getPassword());
        user.setEmail(dto.getEmail()); user.setPhone(dto.getPhone()); user.setAvatar(dto.getAvatar());
        user.setRealName(dto.getRealName()); user.setDepartment(dto.getDepartment()); user.setPosition(dto.getPosition());
        user.setStatus(dto.getStatus());
        if (!createUser(user)) throw new BusinessException("Failed to create user");
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateUser(UserDTO dto) {
        if (dto.getId() == null) throw new BusinessException("User ID is required");
        User user = getById(dto.getId());
        if (user == null) throw new BusinessException("User does not exist");
        if (dto.getUsername() != null && !dto.getUsername().equals(user.getUsername())) {
            User duplicate = getByUsername(dto.getUsername());
            if (duplicate != null && !duplicate.getId().equals(user.getId())) throw new BusinessException("Username already exists");
            user.setUsername(dto.getUsername());
        }
        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            User duplicate = baseMapper.selectByEmail(dto.getEmail());
            if (duplicate != null && !duplicate.getId().equals(user.getId())) throw new BusinessException("Email already exists");
            user.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getAvatar() != null) user.setAvatar(dto.getAvatar());
        if (dto.getRealName() != null) user.setRealName(dto.getRealName());
        if (dto.getDepartment() != null) user.setDepartment(dto.getDepartment());
        if (dto.getPosition() != null) user.setPosition(dto.getPosition());
        if (dto.getStatus() != null) user.setStatus(dto.getStatus());
        return updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteUser(Long userId) {
        if (getById(userId) == null) throw new BusinessException("User does not exist");
        return removeById(userId);
    }

    @Override
    public UserVO getUserById(Long userId) {
        User user = getById(userId);
        if (user == null) throw new BusinessException("User does not exist");
        return toUserVO(user);
    }

    @Override
    public IPage<UserVO> pageUsers(Long current, Long size, String keyword, String role, Integer status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (org.springframework.util.StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword).or().like(User::getRealName, keyword)
                    .or().like(User::getEmail, keyword).or().like(User::getPhone, keyword));
        }
        wrapper.eq(status != null, User::getStatus, status).orderByDesc(User::getCreateTime);
        return page(new Page<>(current, size), wrapper).convert(this::toUserVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean resetPassword(Long userId, String newPassword) {
        if (!org.springframework.util.StringUtils.hasText(newPassword)) throw new BusinessException("New password is required");
        User user = getById(userId);
        if (user == null) throw new BusinessException("User does not exist");
        user.setPassword(passwordEncoder.encode(newPassword));
        return updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean changePassword(String oldPassword, String newPassword) {
        Long userId = UserContextUtil.getUserId();
        if (userId == null) throw new BusinessException(ResultCode.UNAUTHORIZED);
        User user = getById(userId);
        if (user == null || !passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("Old password is incorrect");
        }
        if (!org.springframework.util.StringUtils.hasText(newPassword)) throw new BusinessException("New password is required");
        user.setPassword(passwordEncoder.encode(newPassword));
        return updateById(user);
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
        vo.setLastLoginTime(user.getLastLoginTime()); vo.setLastLoginIp(user.getLastLoginIp());
        vo.setCreateTime(user.getCreateTime()); vo.setUpdateTime(user.getUpdateTime());
        return vo;
    }
}
