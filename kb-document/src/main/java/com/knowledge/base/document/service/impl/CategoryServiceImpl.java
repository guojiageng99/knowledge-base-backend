package com.knowledge.base.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.knowledge.base.common.exception.BusinessException;
import com.knowledge.base.common.utils.SnowflakeIdGenerator;
import com.knowledge.base.document.dto.CategoryDTO;
import com.knowledge.base.document.entity.Category;
import com.knowledge.base.document.mapper.CategoryMapper;
import com.knowledge.base.document.service.CategoryService;
import com.knowledge.base.document.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCategory(CategoryDTO categoryDTO) {
        requireUniqueName(categoryDTO.getName(), null);
        Long parentId = normalizeParentId(categoryDTO.getParentId());
        requireParentExists(parentId);

        Category category = new Category();
        category.setId(SnowflakeIdGenerator.nextId());
        category.setParentId(parentId);
        category.setCategoryName(categoryDTO.getName());
        category.setCategoryCode(StringUtils.hasText(categoryDTO.getCode()) ? categoryDTO.getCode() : generateCategoryCode(categoryDTO.getName()));
        category.setDescription(categoryDTO.getDescription());
        category.setIcon(categoryDTO.getIcon());
        category.setSort(categoryDTO.getSortOrder() == null ? 0 : categoryDTO.getSortOrder());
        category.setStatus(categoryDTO.getStatus() == null ? 1 : categoryDTO.getStatus());
        category.setDocumentCount(0);
        category.setRemark(categoryDTO.getRemark());
        if (categoryMapper.insert(category) <= 0) {
            throw new BusinessException("Failed to create category");
        }
        return category.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateCategory(CategoryDTO categoryDTO) {
        if (categoryDTO.getId() == null) {
            throw new BusinessException("Category ID is required");
        }
        Category existing = requireCategory(categoryDTO.getId());
        if (StringUtils.hasText(categoryDTO.getName())) {
            requireUniqueName(categoryDTO.getName(), categoryDTO.getId());
        }
        Long parentId = categoryDTO.getParentId();
        if (parentId != null) {
            validateMove(categoryDTO.getId(), parentId);
        }

        Category category = new Category();
        category.setId(existing.getId());
        if (StringUtils.hasText(categoryDTO.getCode())) category.setCategoryCode(categoryDTO.getCode());
        if (StringUtils.hasText(categoryDTO.getName())) {
            category.setCategoryName(categoryDTO.getName());
        }
        category.setDescription(categoryDTO.getDescription());
        category.setParentId(parentId == null ? null : normalizeParentId(parentId));
        category.setIcon(categoryDTO.getIcon());
        category.setSort(categoryDTO.getSortOrder());
        category.setStatus(categoryDTO.getStatus());
        category.setRemark(categoryDTO.getRemark());
        return categoryMapper.updateById(category) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteCategory(Long categoryId) {
        requireCategory(categoryId);
        Long childCount = categoryMapper.selectCount(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, categoryId));
        if (childCount > 0) {
            throw new BusinessException("Categories with child categories cannot be deleted");
        }
        return categoryMapper.deleteById(categoryId) > 0;
    }

    @Override
    public CategoryVO getCategoryById(Long categoryId) {
        return toVO(requireCategory(categoryId));
    }

    @Override
    public List<CategoryVO> getCategoryTree() {
        List<CategoryVO> categories = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                        .eq(Category::getStatus, 1)
                        .orderByAsc(Category::getSort)
                        .orderByDesc(Category::getCreateTime))
                .stream()
                .map(this::toVO)
                .toList();
        return buildCategoryTree(categories, 0L);
    }

    @Override
    public List<CategoryVO> getChildren(Long parentId) {
        return categoryMapper.selectByParentId(normalizeParentId(parentId)).stream()
                .filter(category -> Integer.valueOf(1).equals(category.getStatus()))
                .map(this::toVO)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean moveCategory(Long categoryId, Long newParentId) {
        requireCategory(categoryId);
        validateMove(categoryId, newParentId);
        Category update = new Category();
        update.setId(categoryId);
        update.setParentId(normalizeParentId(newParentId));
        return categoryMapper.updateById(update) > 0;
    }

    @Override
    public List<CategoryVO> getAllCategories() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                        .eq(Category::getStatus, 1)
                        .orderByAsc(Category::getSort)
                        .orderByDesc(Category::getCreateTime))
                .stream()
                .map(this::toVO)
                .toList();
    }

    private Category requireCategory(Long categoryId) {
        if (categoryId == null) {
            throw new BusinessException("Category ID is required");
        }
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException("Category does not exist");
        }
        return category;
    }

    private void requireUniqueName(String name, Long excludedId) {
        Category existing = categoryMapper.selectOne(new LambdaQueryWrapper<Category>()
                .eq(Category::getCategoryName, name));
        if (existing != null && !existing.getId().equals(excludedId)) {
            throw new BusinessException("Category name is already in use");
        }
    }

    private void requireParentExists(Long parentId) {
        if (parentId > 0) {
            requireCategory(parentId);
        }
    }

    private void validateMove(Long categoryId, Long newParentId) {
        Long normalizedParentId = normalizeParentId(newParentId);
        if (categoryId.equals(normalizedParentId)) {
            throw new BusinessException("A category cannot be its own parent");
        }
        requireParentExists(normalizedParentId);
        if (normalizedParentId > 0 && isDescendant(categoryId, normalizedParentId)) {
            throw new BusinessException("A category cannot be moved under its descendant");
        }
    }

    private boolean isDescendant(Long ancestorId, Long descendantId) {
        Category category = categoryMapper.selectById(descendantId);
        while (category != null && category.getParentId() != null && category.getParentId() > 0) {
            if (category.getParentId().equals(ancestorId)) {
                return true;
            }
            category = categoryMapper.selectById(category.getParentId());
        }
        return false;
    }

    private List<CategoryVO> buildCategoryTree(List<CategoryVO> categories, Long parentId) {
        List<CategoryVO> tree = new ArrayList<>();
        for (CategoryVO category : categories) {
            if (parentId.equals(category.getParentId())) {
                category.setChildren(buildCategoryTree(categories, category.getId()));
                tree.add(category);
            }
        }
        return tree;
    }

    private CategoryVO toVO(Category category) {
        return CategoryVO.builder()
                .id(category.getId())
                .name(category.getCategoryName())
                .code(category.getCategoryCode())
                .description(category.getDescription())
                .parentId(category.getParentId())
                .sortOrder(category.getSort())
                .icon(category.getIcon())
                .status(category.getStatus())
                .remark(category.getRemark())
                .documentCount(category.getDocumentCount() == null ? 0L : category.getDocumentCount().longValue())
                .createTime(category.getCreateTime())
                .updateTime(category.getUpdateTime())
                .build();
    }

    private Long normalizeParentId(Long parentId) {
        return parentId == null ? 0L : parentId;
    }

    private String generateCategoryCode(String categoryName) {
        return "CAT_" + categoryName.toUpperCase()
                .replaceAll("[^A-Z0-9]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "") + "_" + System.currentTimeMillis();
    }
}
