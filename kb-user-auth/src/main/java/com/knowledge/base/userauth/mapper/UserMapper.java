package com.knowledge.base.userauth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knowledge.base.userauth.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    User selectByUsername(@Param("username") String username);

    User selectByEmail(@Param("email") String email);

    User selectByPhone(@Param("phone") String phone);

    Long countDocumentsByAuthorId(@Param("authorId") Long authorId);
    Long sumLikesByAuthorId(@Param("authorId") Long authorId);
    Long sumViewsByAuthorId(@Param("authorId") Long authorId);
}
