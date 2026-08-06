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
import com.knowledge.base.userauth.entity.Team;
import com.knowledge.base.userauth.entity.TeamMember;
import com.knowledge.base.userauth.dto.RegisterDTO;
import com.knowledge.base.userauth.dto.InviteUserDTO;
import com.knowledge.base.userauth.dto.AcceptInviteDTO;
import com.knowledge.base.userauth.dto.UserDTO;
import com.knowledge.base.userauth.dto.UserProfileDTO;
import com.knowledge.base.userauth.mapper.UserMapper;
import com.knowledge.base.userauth.mapper.RoleMapper;
import com.knowledge.base.userauth.mapper.PermissionMapper;
import com.knowledge.base.userauth.mapper.TeamMapper;
import com.knowledge.base.userauth.mapper.TeamMemberMapper;
import com.knowledge.base.userauth.service.EmailService;
import com.knowledge.base.userauth.service.SecurityConfigService;
import com.knowledge.base.userauth.service.UserService;
import com.knowledge.base.userauth.vo.LoginVO;
import com.knowledge.base.userauth.vo.RegisterVO;
import com.knowledge.base.userauth.vo.UserVO;
import com.knowledge.base.userauth.vo.UserStatisticsVO;
import com.knowledge.base.userauth.util.VerificationTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.security.SecureRandom;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final SecurityConfigService securityConfigService;
    private final EmailService emailService;
    private final VerificationTokenUtil verificationTokenUtil;
    private final StringRedisTemplate stringRedisTemplate;
    private final TeamMapper teamMapper;
    private final TeamMemberMapper teamMemberMapper;

    private static final String RESET_CODE_KEY_PREFIX = "password:reset:code:";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public User getByUsername(String username) {
        return baseMapper.selectByUsername(username);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createUser(User user) {
        if (getByUsername(user.getUsername()) != null) throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        validatePassword(user.getPassword());
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
        user.setRemark(dto.getRemark());
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
        if (dto.getRemark() != null) user.setRemark(dto.getRemark());
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
        validatePassword(newPassword);
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
        validatePassword(newPassword);
        user.setPassword(passwordEncoder.encode(newPassword));
        return updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegisterVO register(RegisterDTO registerDTO) {
        if (!securityConfigService.isRegistrationEnabled()) {
            throw new BusinessException("Registration is currently disabled");
        }
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new BusinessException("Passwords do not match");
        }
        securityConfigService.validatePassword(registerDTO.getPassword());

        String username = registerDTO.getUsername().trim();
        String email = normalizeEmail(registerDTO.getEmail());
        String phone = hasText(registerDTO.getPhone()) ? registerDTO.getPhone().trim() : null;
        if (baseMapper.selectByUsername(username) != null) throw new BusinessException("Username already exists");
        if (baseMapper.selectByEmail(email) != null) throw new BusinessException("Email already registered");
        if (phone != null && baseMapper.selectByPhone(phone) != null) throw new BusinessException("Phone number already registered");

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setEmail(email);
        user.setPhone(phone);
        user.setRealName(registerDTO.getRealName().trim());
        user.setStatus(0);
        user.setEmailVerified(0);
        String activationToken = verificationTokenUtil.generateToken();
        user.setActivationToken(activationToken);
        user.setActivationTokenExpiry(verificationTokenUtil.calculateExpiryTime());
        if (!save(user)) throw new BusinessException("Failed to create user");

        if (registerDTO.getTeamId() != null) {
            Team team = teamMapper.selectById(registerDTO.getTeamId());
            if (team == null || !Integer.valueOf(1).equals(team.getStatus())) {
                throw new BusinessException("Selected team does not exist or is disabled");
            }
            TeamMember member = new TeamMember();
            member.setTeamId(team.getId());
            member.setUserId(user.getId());
            member.setMemberRole("member");
            member.setJoinTime(LocalDateTime.now());
            member.setCreateBy(user.getId());
            if (teamMemberMapper.insert(member) != 1) throw new BusinessException("Failed to join selected team");
            teamMapper.incrementMemberCount(team.getId());
        }

        emailService.sendActivationEmail(user.getEmail(), user.getUsername(), activationToken);
        return RegisterVO.builder().userId(user.getId()).emailVerificationRequired(true)
                .message("Registration succeeded. Please check your email to activate the account.").build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegisterVO inviteUser(InviteUserDTO dto) {
        requireAdministrator();
        String username = dto.getUsername().trim();
        String email = normalizeEmail(dto.getEmail());
        if (baseMapper.selectByUsername(username) != null) throw new BusinessException("Username already exists");
        if (baseMapper.selectByEmail(email) != null) throw new BusinessException("Email already registered");
        Team team = teamMapper.selectById(dto.getTeamId());
        if (team == null || !Integer.valueOf(1).equals(team.getStatus())) throw new BusinessException("Selected team does not exist or is disabled");

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setEmail(email);
        user.setPhone(hasText(dto.getPhone()) ? dto.getPhone().trim() : null);
        user.setRealName(dto.getRealName().trim());
        user.setStatus(0);
        user.setEmailVerified(0);
        user.setActivationToken(verificationTokenUtil.generateToken());
        user.setActivationTokenExpiry(verificationTokenUtil.calculateExpiryTime());
        user.setCreateBy(UserContextUtil.getUserId());
        if (!save(user)) throw new BusinessException("Failed to create invited user");

        TeamMember member = new TeamMember();
        member.setTeamId(team.getId()); member.setUserId(user.getId()); member.setMemberRole("member");
        member.setJoinTime(LocalDateTime.now()); member.setCreateBy(UserContextUtil.getUserId());
        if (teamMemberMapper.insert(member) != 1) throw new BusinessException("Failed to join selected team");
        teamMapper.incrementMemberCount(team.getId());
        emailService.sendInvitationEmail(user.getEmail(), user.getUsername(), user.getActivationToken());
        return RegisterVO.builder().userId(user.getId()).emailVerificationRequired(true)
                .message("Invitation sent. The user must accept it and set a password before signing in.").build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String acceptInvite(AcceptInviteDTO dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) throw new BusinessException("Passwords do not match");
        securityConfigService.validatePassword(dto.getPassword());
        User user = baseMapper.selectByActivationToken(dto.getToken().trim());
        if (user == null) throw new BusinessException("Invalid invitation link");
        if (Integer.valueOf(1).equals(user.getEmailVerified())) throw new BusinessException("Invitation has already been used");
        if (verificationTokenUtil.isTokenExpired(user.getActivationTokenExpiry())) throw new BusinessException("Invitation has expired");
        user.setPassword(passwordEncoder.encode(dto.getPassword())); user.setEmailVerified(1); user.setStatus(1);
        user.setActivationToken(null); user.setActivationTokenExpiry(null);
        if (!updateById(user)) throw new BusinessException("Failed to activate invited account");
        return "Invitation accepted. You can now sign in";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String verifyEmail(String token) {
        if (!hasText(token)) throw new BusinessException("Invalid activation link");
        User user = baseMapper.selectByActivationToken(token.trim());
        if (user == null) throw new BusinessException("Invalid activation link");
        if (Integer.valueOf(1).equals(user.getEmailVerified())) return "Account is already activated";
        if (verificationTokenUtil.isTokenExpired(user.getActivationTokenExpiry())) {
            throw new BusinessException("Activation link has expired. Please register again");
        }
        user.setEmailVerified(1);
        user.setStatus(1);
        user.setActivationToken(null);
        user.setActivationTokenExpiry(null);
        if (!updateById(user)) throw new BusinessException("Failed to activate account");
        return "Account activated successfully. You can now sign in";
    }

    @Override
    public void sendResetCode(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (baseMapper.selectByEmail(normalizedEmail) == null) throw new BusinessException("Email is not registered");
        String redisKey = resetCodeKey(normalizedEmail);
        Long ttl = stringRedisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
        if (ttl != null && ttl > 540) {
            throw new BusinessException("A verification code was already sent. Try again in " + (ttl - 540) + " seconds");
        }
        String code = String.format(Locale.ROOT, "%06d", SECURE_RANDOM.nextInt(1_000_000));
        stringRedisTemplate.opsForValue().set(redisKey, code, 10, TimeUnit.MINUTES);
        emailService.sendResetCodeEmail(normalizedEmail, code);
    }

    @Override
    public boolean verifyResetCode(String email, String code) {
        String storedCode = stringRedisTemplate.opsForValue().get(resetCodeKey(normalizeEmail(email)));
        if (storedCode == null) throw new BusinessException("Verification code has expired. Request a new one");
        if (!storedCode.equals(code)) throw new BusinessException("Invalid verification code");
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(String email, String code, String newPassword) {
        String normalizedEmail = normalizeEmail(email);
        verifyResetCode(normalizedEmail, code);
        securityConfigService.validatePassword(newPassword);
        User user = baseMapper.selectByEmail(normalizedEmail);
        if (user == null) throw new BusinessException("User does not exist");
        user.setPassword(passwordEncoder.encode(newPassword));
        if (!updateById(user)) throw new BusinessException("Failed to reset password");
        stringRedisTemplate.delete(resetCodeKey(normalizedEmail));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateCurrentUserProfile(UserProfileDTO dto) {
        Long userId = UserContextUtil.getUserId();
        if (userId == null) throw new BusinessException(ResultCode.UNAUTHORIZED);
        User user = getById(userId);
        if (user == null || !Integer.valueOf(1).equals(user.getStatus())) throw new BusinessException(ResultCode.UNAUTHORIZED);
        if (dto.getUsername() != null && !dto.getUsername().equals(user.getUsername())) {
            User duplicate = getByUsername(dto.getUsername());
            if (duplicate != null && !duplicate.getId().equals(userId)) throw new BusinessException("Username already exists");
            user.setUsername(dto.getUsername());
        }
        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            User duplicate = baseMapper.selectByEmail(dto.getEmail());
            if (duplicate != null && !duplicate.getId().equals(userId)) throw new BusinessException("Email already exists");
            user.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getAvatar() != null) user.setAvatar(dto.getAvatar());
        if (dto.getRealName() != null) user.setRealName(dto.getRealName());
        if (dto.getDepartment() != null) user.setDepartment(dto.getDepartment());
        if (dto.getPosition() != null) user.setPosition(dto.getPosition());
        if (dto.getRemark() != null) user.setRemark(dto.getRemark());
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

    @Override
    public UserStatisticsVO getUserStatistics(Long userId) {
        if (userId == null || getById(userId) == null) throw new BusinessException("User does not exist");
        return UserStatisticsVO.builder()
                .documentCount(zeroIfNull(baseMapper.countDocumentsByAuthorId(userId)))
                .likeCount(zeroIfNull(baseMapper.sumLikesByAuthorId(userId)))
                .viewCount(zeroIfNull(baseMapper.sumViewsByAuthorId(userId)))
                .commentCount(0L)
                .build();
    }

    private long zeroIfNull(Long value) { return value == null ? 0L : value; }

    private void validatePassword(String password) {
        securityConfigService.validatePassword(password);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String resetCodeKey(String email) { return RESET_CODE_KEY_PREFIX + email; }

    private boolean hasText(String value) { return value != null && !value.trim().isEmpty(); }

    private void requireAdministrator() {
        Long userId = UserContextUtil.getUserId();
        if (userId == null || roleMapper.selectRoleCodesByUserId(userId).stream()
                .noneMatch(role -> "admin".equalsIgnoreCase(role)
                        || "role_admin".equalsIgnoreCase(role)
                        || "role_super_admin".equalsIgnoreCase(role))) {
            throw new BusinessException("Administrator permission is required");
        }
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
                .nickname(user.getRealName()).email(user.getEmail()).phone(user.getPhone()).avatar(user.getAvatar())
                .roles(roleMapper.selectRoleCodesByUserId(user.getId()))
                .permissions(permissionMapper.selectPermissionCodesByUserId(user.getId())).build();
    }

    private UserVO toUserVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId()); vo.setUsername(user.getUsername()); vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone()); vo.setAvatar(user.getAvatar()); vo.setRealName(user.getRealName());
        vo.setDepartment(user.getDepartment()); vo.setPosition(user.getPosition()); vo.setStatus(user.getStatus());
        vo.setRemark(user.getRemark());
        vo.setLastLoginTime(user.getLastLoginTime()); vo.setLastLoginIp(user.getLastLoginIp());
        vo.setCreateTime(user.getCreateTime()); vo.setUpdateTime(user.getUpdateTime());
        vo.setRoles(roleMapper.selectRoleCodesByUserId(user.getId()));
        vo.setPermissions(permissionMapper.selectPermissionCodesByUserId(user.getId()));
        return vo;
    }
}
