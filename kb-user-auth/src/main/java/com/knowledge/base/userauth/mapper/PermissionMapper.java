package com.knowledge.base.userauth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knowledge.base.userauth.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
    @Select("SELECT DISTINCT p.permission_code FROM sys_permission p "
            + "JOIN sys_role_permission rp ON rp.permission_id = p.id "
            + "JOIN sys_user_role ur ON ur.role_id = rp.role_id "
            + "WHERE ur.user_id = #{userId} AND p.status = 1 AND p.deleted = 0")
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);
}
