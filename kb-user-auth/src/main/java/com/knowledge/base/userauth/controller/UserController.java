package com.knowledge.base.userauth.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.knowledge.base.common.result.Result;
import com.knowledge.base.userauth.dto.UserDTO;
import com.knowledge.base.userauth.service.UserService;
import com.knowledge.base.userauth.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/users", "/user"})
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public Result<Long> createUser(@Valid @RequestBody UserDTO dto) { return Result.success("User created successfully", userService.createUser(dto)); }

    @PutMapping
    public Result<Boolean> updateUser(@Valid @RequestBody UserDTO dto) { return Result.success("User updated successfully", userService.updateUser(dto)); }

    @DeleteMapping("/{userId}")
    public Result<Boolean> deleteUser(@PathVariable Long userId) { return Result.success("User deleted successfully", userService.deleteUser(userId)); }

    @GetMapping("/{userId}")
    public Result<UserVO> getUserById(@PathVariable Long userId) { return Result.success(userService.getUserById(userId)); }

    @GetMapping("/page")
    public Result<IPage<UserVO>> pageUsers(@RequestParam(defaultValue = "1") Long current, @RequestParam(defaultValue = "10") Long size, @RequestParam(required = false) String keyword, @RequestParam(required = false) String role, @RequestParam(required = false) Integer status) {
        return Result.success(userService.pageUsers(current, size, keyword, role, status));
    }

    @PutMapping("/{userId}/password/reset")
    public Result<Boolean> resetPassword(@PathVariable Long userId, @RequestParam String newPassword) { return Result.success(userService.resetPassword(userId, newPassword)); }

    @PutMapping("/password/change")
    public Result<Boolean> changePassword(@RequestParam String oldPassword, @RequestParam String newPassword) { return Result.success(userService.changePassword(oldPassword, newPassword)); }
}
