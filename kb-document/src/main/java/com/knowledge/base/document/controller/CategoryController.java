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
@Tag(name = "Category Management", description = "Document category APIs")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(summary = "Create category")
    public Result<Long> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        return Result.success("Category created successfully", categoryService.createCategory(categoryDTO));
    }

    @PutMapping
    @Operation(summary = "Update category")
    public Result<Boolean> updateCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        return Result.success("Category updated successfully", categoryService.updateCategory(categoryDTO));
    }

    @DeleteMapping("/{categoryId}")
    @Operation(summary = "Delete category")
    public Result<Boolean> deleteCategory(@PathVariable Long categoryId) {
        return Result.success("Category deleted successfully", categoryService.deleteCategory(categoryId));
    }

    @GetMapping("/tree")
    @Operation(summary = "Get category tree")
    public Result<List<CategoryVO>> getCategoryTree() {
        return Result.success(categoryService.getCategoryTree());
    }

    @GetMapping("/children/{parentId}")
    @Operation(summary = "Get child categories")
    public Result<List<CategoryVO>> getChildren(@PathVariable Long parentId) {
        return Result.success(categoryService.getChildren(parentId));
    }

    @PutMapping("/{categoryId}/move")
    @Operation(summary = "Move category")
    public Result<Boolean> moveCategory(
            @PathVariable Long categoryId,
            @Parameter(required = true) @RequestParam Long newParentId) {
        return Result.success("Category moved successfully", categoryService.moveCategory(categoryId, newParentId));
    }

    @GetMapping("/list")
    @Operation(summary = "Get all categories")
    public Result<List<CategoryVO>> getAllCategories() {
        return Result.success(categoryService.getAllCategories());
    }

    @GetMapping("/{categoryId}")
    @Operation(summary = "Get category by ID")
    public Result<CategoryVO> getCategoryById(@PathVariable Long categoryId) {
        return Result.success(categoryService.getCategoryById(categoryId));
    }
}
