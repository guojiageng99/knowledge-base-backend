package com.knowledge.base.document.controller;

import com.knowledge.base.common.result.Result;
import com.knowledge.base.document.dto.CategoryDTO;
import com.knowledge.base.document.service.CategoryService;
import com.knowledge.base.document.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(name = "分类管理", description = "文档分类管理接口")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(summary = "创建分类", description = "创建新的文档分类")
    public Result<Long> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        return Result.success("创建分类成功", categoryService.createCategory(categoryDTO));
    }

    @PutMapping
    @Operation(summary = "更新分类", description = "更新分类信息")
    public Result<Boolean> updateCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        return Result.success("更新分类成功", categoryService.updateCategory(categoryDTO));
    }

    @DeleteMapping("/{categoryId}")
    @Operation(summary = "删除分类", description = "根据分类ID删除分类")
    public Result<Boolean> deleteCategory(
            @Parameter(description = "分类ID", required = true) @PathVariable Long categoryId) {
        return Result.success("删除分类成功", categoryService.deleteCategory(categoryId));
    }

    @GetMapping("/tree")
    @Operation(summary = "获取分类树", description = "获取完整的分类树结构")
    public Result<List<CategoryVO>> getCategoryTree() {
        return Result.success(categoryService.getCategoryTree());
    }

    @GetMapping("/children/{parentId}")
    @Operation(summary = "获取子分类", description = "获取指定父分类的子分类列表")
    public Result<List<CategoryVO>> getChildren(
            @Parameter(description = "父分类ID", required = true) @PathVariable Long parentId) {
        return Result.success(categoryService.getChildren(parentId));
    }

    @PutMapping("/{categoryId}/move")
    @Operation(summary = "移动分类", description = "移动分类到新的父分类下")
    public Result<Boolean> moveCategory(
            @Parameter(description = "分类ID", required = true) @PathVariable Long categoryId,
            @Parameter(description = "新父分类ID", required = true) @RequestParam Long newParentId) {
        return Result.success("移动分类成功", categoryService.moveCategory(categoryId, newParentId));
    }

    @GetMapping("/list")
    @Operation(summary = "获取所有分类", description = "获取所有分类列表（平铺）")
    public Result<List<CategoryVO>> getAllCategories() {
        return Result.success(categoryService.getAllCategories());
    }

    @GetMapping("/{categoryId}")
    @Operation(summary = "查询分类", description = "根据分类ID查询分类详情")
    public Result<CategoryVO> getCategoryById(
            @Parameter(description = "分类ID", required = true) @PathVariable Long categoryId) {
        return Result.success(categoryService.getCategoryById(categoryId));
    }
}
