package com.knowledge.base.userauth.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.knowledge.base.userauth.dto.PermissionDTO;
import com.knowledge.base.userauth.vo.PermissionVO;

import java.util.List;

public interface PermissionService {
    Long createPermission(PermissionDTO dto);
    Boolean updatePermission(PermissionDTO dto);
    Boolean deletePermission(Long permissionId);
    PermissionVO getPermissionById(Long permissionId);
    List<PermissionVO> getPermissionsByParentId(Long parentId);
    IPage<PermissionVO> pagePermissions(Long current, Long size, String keyword);
    List<PermissionVO> getPermissionTree();
    List<PermissionVO> getAllPermissions();
}
