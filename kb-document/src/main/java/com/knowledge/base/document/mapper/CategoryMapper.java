package com.knowledge.base.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knowledge.base.document.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    List<Category> selectByParentId(@Param("parentId") Long parentId);

    int updateDocumentCount(@Param("categoryId") Long categoryId, @Param("count") Integer count);

    int incrementDocumentCount(@Param("categoryId") Long categoryId);

    int decrementDocumentCount(@Param("categoryId") Long categoryId);
}
