package com.knowledge.base.userauth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.knowledge.base.common.exception.BusinessException;
import com.knowledge.base.userauth.dto.PermissionDTO;
import com.knowledge.base.userauth.entity.Permission;
import com.knowledge.base.userauth.mapper.PermissionMapper;
import com.knowledge.base.userauth.service.PermissionService;
import com.knowledge.base.userauth.vo.PermissionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {
    private static final long ROOT_ID = 0L;
    private final PermissionMapper permissionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPermission(PermissionDTO dto) {
        ensureCodeAvailable(dto.getCode(), null);
        ensureParentValid(dto.getParentId(), null);
        Permission permission = fromDTO(dto, new Permission());
        permission.setParentId(dto.getParentId() == null ? ROOT_ID : dto.getParentId());
        permission.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        permission.setSort(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        if (permissionMapper.insert(permission) != 1) throw new BusinessException("Failed to create permission");
        return permission.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updatePermission(PermissionDTO dto) {
        if (dto.getId() == null) throw new BusinessException("Permission ID is required");
        Permission existing = requirePermission(dto.getId());
        ensureCodeAvailable(dto.getCode(), dto.getId());
        ensureParentValid(dto.getParentId(), dto.getId());
        Permission permission = fromDTO(dto, existing);
        permission.setParentId(dto.getParentId() == null ? ROOT_ID : dto.getParentId());
        return permissionMapper.updateById(permission) == 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deletePermission(Long permissionId) {
        requirePermission(permissionId);
        if (permissionMapper.selectCount(new LambdaQueryWrapper<Permission>()
                .eq(Permission::getParentId, permissionId)) > 0) {
            throw new BusinessException("Permissions with child resources cannot be deleted");
        }
        return permissionMapper.deleteById(permissionId) == 1;
    }

    @Override
    public PermissionVO getPermissionById(Long permissionId) {
        return toVO(requirePermission(permissionId));
    }

    @Override
    public List<PermissionVO> getPermissionsByParentId(Long parentId) {
        return permissionMapper.selectList(new LambdaQueryWrapper<Permission>()
                        .eq(Permission::getParentId, parentId == null ? ROOT_ID : parentId)
                        .orderByAsc(Permission::getSort))
                .stream().map(this::toVO).toList();
    }

    @Override
    public IPage<PermissionVO> pagePermissions(Long current, Long size, String keyword) {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<Permission>().orderByAsc(Permission::getSort);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(query -> query.like(Permission::getPermissionName, keyword)
                    .or().like(Permission::getPermissionCode, keyword));
        }
        return permissionMapper.selectPage(new Page<>(current, size), wrapper).convert(this::toVO);
    }

    @Override
    public List<PermissionVO> getPermissionTree() {
        List<PermissionVO> permissions = permissionMapper.selectList(new LambdaQueryWrapper<Permission>()
                        .orderByAsc(Permission::getSort))
                .stream().map(this::toVO).toList();
        return buildTree(permissions, ROOT_ID);
    }

    @Override
    public List<PermissionVO> getAllPermissions() {
        return permissionMapper.selectList(new LambdaQueryWrapper<Permission>()
                        .eq(Permission::getStatus, 1).orderByAsc(Permission::getSort))
                .stream().map(this::toVO).toList();
    }

    private Permission requirePermission(Long permissionId) {
        if (permissionId == null) throw new BusinessException("Permission ID is required");
        Permission permission = permissionMapper.selectById(permissionId);
        if (permission == null) throw new BusinessException("Permission does not exist");
        return permission;
    }

    private void ensureCodeAvailable(String code, Long currentId) {
        Permission duplicate = permissionMapper.selectOne(new LambdaQueryWrapper<Permission>()
                .eq(Permission::getPermissionCode, code));
        if (duplicate != null && !duplicate.getId().equals(currentId)) {
            throw new BusinessException("Permission code already exists");
        }
    }

    private void ensureParentValid(Long parentId, Long currentId) {
        if (parentId == null || parentId == ROOT_ID) return;
        if (parentId.equals(currentId)) throw new BusinessException("A permission cannot be its own parent");
        if (permissionMapper.selectById(parentId) == null) throw new BusinessException("Parent permission does not exist");
    }

    private Permission fromDTO(PermissionDTO dto, Permission permission) {
        permission.setPermissionName(dto.getName());
        permission.setPermissionCode(dto.getCode());
        if (StringUtils.hasText(dto.getType())) permission.setPermissionType(toTypeValue(dto.getType()));
        else if (permission.getPermissionType() == null) permission.setPermissionType(1);
        permission.setMenuUrl(dto.getMenuUrl());
        permission.setApiUrl(dto.getApiUrl());
        permission.setMethod(dto.getMethod());
        permission.setIcon(dto.getIcon());
        permission.setRemark(dto.getDescription());
        if (dto.getSortOrder() != null) permission.setSort(dto.getSortOrder());
        if (dto.getStatus() != null) permission.setStatus(dto.getStatus());
        return permission;
    }

    private PermissionVO toVO(Permission permission) {
        return PermissionVO.builder().id(permission.getId()).name(permission.getPermissionName())
                .code(permission.getPermissionCode()).type(toTypeName(permission.getPermissionType()))
                .parentId(permission.getParentId()).menuUrl(permission.getMenuUrl()).apiUrl(permission.getApiUrl())
                .method(permission.getMethod()).icon(permission.getIcon()).description(permission.getRemark())
                .sortOrder(permission.getSort()).status(permission.getStatus()).createTime(permission.getCreateTime())
                .updateTime(permission.getUpdateTime()).build();
    }

    private List<PermissionVO> buildTree(List<PermissionVO> permissions, Long parentId) {
        List<PermissionVO> result = new ArrayList<>();
        for (PermissionVO permission : permissions) {
            if (parentId.equals(permission.getParentId())) {
                permission.setChildren(buildTree(permissions, permission.getId()));
                result.add(permission);
            }
        }
        return result;
    }

    private int toTypeValue(String type) {
        return switch (type.trim().toLowerCase()) {
            case "button", "2" -> 2;
            case "api", "3" -> 3;
            default -> 1;
        };
    }

    private String toTypeName(Integer type) {
        return switch (type == null ? 1 : type) {
            case 2 -> "button";
            case 3 -> "api";
            default -> "menu";
        };
    }
}
