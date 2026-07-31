package com.knowledge.base.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.knowledge.base.common.exception.BusinessException;
import com.knowledge.base.common.result.PageResult;
import com.knowledge.base.common.utils.SnowflakeIdGenerator;
import com.knowledge.base.document.dto.TagCreateDTO;
import com.knowledge.base.document.dto.TagQueryDTO;
import com.knowledge.base.document.dto.TagUpdateDTO;
import com.knowledge.base.document.entity.Tag;
import com.knowledge.base.document.mapper.TagMapper;
import com.knowledge.base.document.service.TagService;
import com.knowledge.base.document.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    private final TagMapper tagMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTag(TagCreateDTO dto) {
        requireUniqueName(dto.getTagName(), null);
        String tagCode = StringUtils.hasText(dto.getTagCode()) ? dto.getTagCode() : generateTagCode(dto.getTagName());
        if (tagMapper.selectByTagCode(tagCode) != null) {
            throw new BusinessException("Tag code is already in use");
        }

        Tag tag = new Tag();
        tag.setId(SnowflakeIdGenerator.nextId());
        tag.setTagName(dto.getTagName().trim());
        tag.setTagCode(tagCode);
        tag.setCategoryId(dto.getCategoryId());
        tag.setTagType(dto.getTagType() == null ? 1 : dto.getTagType());
        tag.setColor(dto.getColor());
        tag.setIcon(dto.getIcon());
        tag.setDocCount(0);
        tag.setStatus(1);
        tag.setVersion(0);
        if (tagMapper.insert(tag) <= 0) {
            throw new BusinessException("Failed to create tag");
        }
        return tag.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateTag(TagUpdateDTO dto) {
        Tag existing = requireTag(dto.getId());
        if (StringUtils.hasText(dto.getTagName())) {
            requireUniqueName(dto.getTagName(), dto.getId());
        }
        if (StringUtils.hasText(dto.getTagCode()) && !dto.getTagCode().equals(existing.getTagCode())) {
            Tag codeOwner = tagMapper.selectByTagCode(dto.getTagCode());
            if (codeOwner != null && !codeOwner.getId().equals(dto.getId())) {
                throw new BusinessException("Tag code is already in use");
            }
        }

        Tag tag = new Tag();
        tag.setId(dto.getId());
        tag.setTagName(dto.getTagName());
        tag.setTagCode(dto.getTagCode());
        tag.setCategoryId(dto.getCategoryId());
        tag.setColor(dto.getColor());
        tag.setIcon(dto.getIcon());
        tag.setStatus(dto.getStatus());
        return tagMapper.updateById(tag) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteTag(Long tagId) {
        Tag tag = requireTag(tagId);
        if (tag.getDocCount() != null && tag.getDocCount() > 0) {
            throw new BusinessException("Tags associated with documents cannot be deleted");
        }
        return tagMapper.deleteById(tagId) > 0;
    }

    @Override
    public TagVO getTagDetail(Long tagId) {
        return toVO(requireTag(tagId));
    }

    @Override
    public PageResult<TagVO> pageTags(TagQueryDTO dto) {
        LambdaQueryWrapper<Tag> query = new LambdaQueryWrapper<Tag>()
                .and(StringUtils.hasText(dto.getTagName()), wrapper -> wrapper.like(Tag::getTagName, dto.getTagName())
                        .or().like(Tag::getTagCode, dto.getTagName()))
                .eq(dto.getCategoryId() != null, Tag::getCategoryId, dto.getCategoryId())
                .eq(dto.getTagType() != null, Tag::getTagType, dto.getTagType())
                .eq(dto.getStatus() != null, Tag::getStatus, dto.getStatus())
                .eq(dto.getStatus() == null, Tag::getStatus, 1)
                .orderByDesc(Tag::getDocCount);
        IPage<Tag> page = tagMapper.selectPage(new Page<>(dto.getCurrent(), dto.getSize()), query);
        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords().stream().map(this::toVO).toList());
    }

    @Override
    public List<TagVO> getHotTags(Integer limit) {
        int safeLimit = limit == null || limit <= 0 ? 10 : Math.min(limit, 100);
        return tagMapper.selectHotTags(safeLimit).stream().map(this::toVO).toList();
    }

    @Override
    public List<TagVO> getTagsByCategory(Long categoryId) {
        if (categoryId == null) {
            throw new BusinessException("Category ID is required");
        }
        return tagMapper.selectByCategoryId(categoryId).stream()
                .filter(tag -> Integer.valueOf(1).equals(tag.getStatus()))
                .map(this::toVO)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> batchCreateTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> tagIds = new ArrayList<>();
        for (String tagName : tagNames) {
            if (!StringUtils.hasText(tagName)) {
                continue;
            }
            Tag tag = tagMapper.selectOne(new LambdaQueryWrapper<Tag>().eq(Tag::getTagName, tagName.trim()));
            if (tag == null) {
                TagCreateDTO dto = new TagCreateDTO();
                dto.setTagName(tagName.trim());
                dto.setTagType(1);
                tagIds.add(createTag(dto));
            } else {
                tagIds.add(tag.getId());
            }
        }
        return tagIds;
    }

    private Tag requireTag(Long tagId) {
        if (tagId == null) {
            throw new BusinessException("Tag ID is required");
        }
        Tag tag = tagMapper.selectById(tagId);
        if (tag == null) {
            throw new BusinessException("Tag does not exist");
        }
        return tag;
    }

    private void requireUniqueName(String tagName, Long excludedId) {
        Tag existing = tagMapper.selectOne(new LambdaQueryWrapper<Tag>().eq(Tag::getTagName, tagName.trim()));
        if (existing != null && !existing.getId().equals(excludedId)) {
            throw new BusinessException("Tag name is already in use");
        }
    }

    private TagVO toVO(Tag tag) {
        return TagVO.builder()
                .id(tag.getId())
                .tagName(tag.getTagName())
                .tagCode(tag.getTagCode())
                .categoryId(tag.getCategoryId())
                .tagType(tag.getTagType())
                .color(tag.getColor())
                .icon(tag.getIcon())
                .docCount(tag.getDocCount() == null ? 0 : tag.getDocCount())
                .status(tag.getStatus())
                .createdAt(tag.getCreateTime())
                .build();
    }

    private String generateTagCode(String tagName) {
        return "TAG_" + tagName.toUpperCase()
                .replaceAll("[^A-Z0-9]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "") + "_" + System.currentTimeMillis();
    }
}
