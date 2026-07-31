package com.knowledge.base.document.controller;

import com.knowledge.base.common.result.PageResult;
import com.knowledge.base.common.result.Result;
import com.knowledge.base.document.dto.TagCreateDTO;
import com.knowledge.base.document.dto.TagQueryDTO;
import com.knowledge.base.document.dto.TagUpdateDTO;
import com.knowledge.base.document.service.TagService;
import com.knowledge.base.document.vo.TagVO;
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
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Tag(name = "标签管理", description = "标签管理相关接口")
public class TagController {

    private final TagService tagService;

    @PostMapping
    @Operation(summary = "创建标签", description = "创建新标签")
    public Result<Long> createTag(@Valid @RequestBody TagCreateDTO dto) {
        return Result.success("Tag created successfully", tagService.createTag(dto));
    }

    @PutMapping
    @Operation(summary = "更新标签", description = "更新标签信息")
    public Result<Boolean> updateTag(@Valid @RequestBody TagUpdateDTO dto) {
        return Result.success("Tag updated successfully", tagService.updateTag(dto));
    }

    @DeleteMapping("/{tagId}")
    @Operation(summary = "删除标签", description = "删除指定标签")
    public Result<Boolean> deleteTag(
            @Parameter(description = "标签ID", required = true) @PathVariable Long tagId) {
        return Result.success("Tag deleted successfully", tagService.deleteTag(tagId));
    }

    @GetMapping("/hot")
    @Operation(summary = "获取热门标签", description = "获取使用最多的标签")
    public Result<List<TagVO>> getHotTags(
            @Parameter(description = "数量限制") @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(tagService.getHotTags(limit));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "根据分类获取标签", description = "获取指定分类下的标签")
    public Result<List<TagVO>> getTagsByCategory(
            @Parameter(description = "分类ID", required = true) @PathVariable Long categoryId) {
        return Result.success(tagService.getTagsByCategory(categoryId));
    }

    @GetMapping("/search")
    @Operation(summary = "搜索标签", description = "根据关键词搜索标签")
    public Result<List<TagVO>> searchTags(
            @Parameter(description = "搜索关键词", required = true) @RequestParam String keyword) {
        TagQueryDTO dto = new TagQueryDTO();
        dto.setTagName(keyword);
        dto.setCurrent(1L);
        dto.setSize(20L);
        return Result.success(tagService.pageTags(dto).getRecords());
    }

    @PostMapping("/page")
    @Operation(summary = "分页查询标签", description = "分页查询标签列表")
    public Result<PageResult<TagVO>> pageTags(@RequestBody TagQueryDTO dto) {
        return Result.success(tagService.pageTags(dto));
    }

    @GetMapping("/{tagId}")
    @Operation(summary = "获取标签详情", description = "根据ID获取标签详情")
    public Result<TagVO> getTagDetail(
            @Parameter(description = "标签ID", required = true) @PathVariable Long tagId) {
        return Result.success(tagService.getTagDetail(tagId));
    }
}
