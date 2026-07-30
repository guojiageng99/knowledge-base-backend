package com.knowledge.base.document.service;

import com.knowledge.base.document.dto.CategoryDTO;
import com.knowledge.base.document.vo.CategoryVO;

import java.util.List;

public interface CategoryService {

    Long createCategory(CategoryDTO categoryDTO);

    Boolean updateCategory(CategoryDTO categoryDTO);

    Boolean deleteCategory(Long categoryId);

    CategoryVO getCategoryById(Long categoryId);

    List<CategoryVO> getCategoryTree();

    List<CategoryVO> getChildren(Long parentId);

    Boolean moveCategory(Long categoryId, Long newParentId);

    List<CategoryVO> getAllCategories();
}
