package com.knowledge.base.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knowledge.base.document.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    List<Tag> selectByCategoryId(@Param("categoryId") Long categoryId);

    Tag selectByTagCode(@Param("tagCode") String tagCode);

    List<Tag> selectHotTags(@Param("limit") Integer limit);

    int updateDocumentCount(@Param("tagId") Long tagId, @Param("count") Integer count);

    int incrementDocumentCount(@Param("tagId") Long tagId);

    int decrementDocumentCount(@Param("tagId") Long tagId);

    List<Tag> searchByName(@Param("keyword") String keyword);
}
