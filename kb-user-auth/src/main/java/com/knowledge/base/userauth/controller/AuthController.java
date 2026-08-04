package com.knowledge.base.userauth.controller;

import com.knowledge.base.common.result.Result;
import com.knowledge.base.userauth.dto.LoginDTO;
import com.knowledge.base.userauth.dto.RegisterDTO;
import com.knowledge.base.userauth.dto.ResetPasswordDTO;
import com.knowledge.base.userauth.dto.SendResetCodeDTO;
import com.knowledge.base.userauth.dto.VerifyResetCodeDTO;
import com.knowledge.base.userauth.service.UserService;
import com.knowledge.base.userauth.vo.LoginVO;
import com.knowledge.base.userauth.vo.RegisterVO;
import com.knowledge.base.userauth.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户认证相关接口")
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success("登录成功", userService.login(loginDTO.getUsername(), loginDTO.getPassword()));
    }

    @PostMapping("/register")
    @Operation(summary = "User registration")
    public Result<RegisterVO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        return Result.success(userService.register(registerDTO));
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Verify email and activate account")
    public Result<String> verifyEmail(@RequestParam String token) {
        return Result.success(userService.verifyEmail(token));
    }

    @PostMapping("/password/reset/send-code")
    @Operation(summary = "Send password-reset verification code")
    public Result<Void> sendResetCode(@Valid @RequestBody SendResetCodeDTO dto) {
        userService.sendResetCode(dto.getEmail());
        return Result.success("Verification code sent to your email");
    }

    @PostMapping("/password/reset/verify-code")
    @Operation(summary = "Verify password-reset code")
    public Result<Boolean> verifyResetCode(@Valid @RequestBody VerifyResetCodeDTO dto) {
        return Result.success("Verification succeeded", userService.verifyResetCode(dto.getEmail(), dto.getCode()));
    }

    @PostMapping("/password/reset")
    @Operation(summary = "Reset password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        userService.resetPassword(dto.getEmail(), dto.getCode(), dto.getNewPassword());
        return Result.success("Password reset successfully");
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息")
    public Result<UserVO> getCurrentUser() {
        return Result.success(userService.getCurrentUserInfo());
    }

    @PostMapping("/logout")
    @Operation(summary = "用户退出")
    public Result<Void> logout(@RequestHeader("Authorization") String authorization) {
        userService.logout(authorization);
        return Result.success();
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新Token")
    public Result<LoginVO> refresh(@RequestParam String refreshToken) {
        return Result.success(userService.refresh(refreshToken));
    }

    @GetMapping("/test")
    public Result<String> test() {
        return Result.success("测试响应成功", "这是测试数据");
    }
}
