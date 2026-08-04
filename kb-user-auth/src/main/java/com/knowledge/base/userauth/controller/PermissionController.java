package com.knowledge.base.userauth.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.knowledge.base.common.result.Result;
import com.knowledge.base.userauth.dto.PermissionDTO;
import com.knowledge.base.userauth.service.PermissionService;
import com.knowledge.base.userauth.vo.PermissionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
@Tag(name = "Permission management")
public class PermissionController {
    private final PermissionService permissionService;

    @PostMapping
    @Operation(summary = "Create permission")
    public Result<Long> create(@Valid @RequestBody PermissionDTO dto) {
        return Result.success("Permission created", permissionService.createPermission(dto));
    }

    @PutMapping
    @Operation(summary = "Update permission")
    public Result<Boolean> update(@Valid @RequestBody PermissionDTO dto) {
        return Result.success("Permission updated", permissionService.updatePermission(dto));
    }

    @DeleteMapping("/{permissionId}")
    @Operation(summary = "Delete permission")
    public Result<Boolean> delete(@PathVariable Long permissionId) {
        return Result.success("Permission deleted", permissionService.deletePermission(permissionId));
    }

    @GetMapping("/{permissionId}")
    public Result<PermissionVO> detail(@PathVariable Long permissionId) {
        return Result.success(permissionService.getPermissionById(permissionId));
    }

    @GetMapping("/{permissionId}/children")
    public Result<List<PermissionVO>> children(@PathVariable Long permissionId) {
        return Result.success(permissionService.getPermissionsByParentId(permissionId));
    }

    @GetMapping("/page")
    public Result<IPage<PermissionVO>> page(@RequestParam(defaultValue = "1") Long current,
                                             @RequestParam(defaultValue = "10") Long size,
                                             @RequestParam(required = false) String keyword) {
        return Result.success(permissionService.pagePermissions(current, size, keyword));
    }

    @GetMapping("/tree")
    public Result<List<PermissionVO>> tree() {
        return Result.success(permissionService.getPermissionTree());
    }

    @GetMapping("/list")
    public Result<List<PermissionVO>> list() {
        return Result.success(permissionService.getAllPermissions());
    }
}
