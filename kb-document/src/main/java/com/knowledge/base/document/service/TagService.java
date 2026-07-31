package com.knowledge.base.document.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.knowledge.base.common.result.PageResult;
import com.knowledge.base.document.dto.TagCreateDTO;
import com.knowledge.base.document.dto.TagQueryDTO;
import com.knowledge.base.document.dto.TagUpdateDTO;
import com.knowledge.base.document.entity.Tag;
import com.knowledge.base.document.vo.TagVO;

import java.util.List;

public interface TagService extends IService<Tag> {

    Long createTag(TagCreateDTO dto);
    Boolean updateTag(TagUpdateDTO dto);
    Boolean deleteTag(Long tagId);
    TagVO getTagDetail(Long tagId);
    PageResult<TagVO> pageTags(TagQueryDTO dto);
    List<TagVO> getHotTags(Integer limit);
    List<TagVO> getTagsByCategory(Long categoryId);
    List<Long> batchCreateTags(List<String> tagNames);
}
