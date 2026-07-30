package com.knowledge.base.foundation.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.knowledge.base.common.result.Result;
import com.knowledge.base.foundation.entity.Dict;
import com.knowledge.base.foundation.entity.DictData;
import com.knowledge.base.foundation.service.DictService;
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
@RequestMapping("/dicts")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    @GetMapping
    public Result<IPage<Dict>> page(@RequestParam(defaultValue = "1") Long current,
                                    @RequestParam(defaultValue = "10") Long size,
                                    @RequestParam(required = false) String keyword) {
        return Result.success(dictService.pageDicts(current, size, keyword));
    }

    @GetMapping("/{code}")
    public Result<Dict> getByCode(@PathVariable String code) {
        return Result.success(dictService.getDictByCode(code));
    }

    @PostMapping
    public Result<Boolean> create(@Valid @RequestBody Dict dict) {
        return Result.success(dictService.createDict(dict));
    }

    @PutMapping("/{code}")
    public Result<Boolean> update(@PathVariable String code, @RequestBody Dict dict) {
        return Result.success(dictService.updateDict(code, dict));
    }

    @DeleteMapping("/{code}")
    public Result<Boolean> delete(@PathVariable String code) {
        return Result.success(dictService.deleteDict(code));
    }

    @GetMapping("/{code}/data")
    public Result<List<DictData>> getData(@PathVariable String code) {
        return Result.success(dictService.getDictData(code));
    }

    @PostMapping("/{code}/data")
    public Result<Boolean> addData(@PathVariable String code, @RequestBody DictData dictData) {
        return Result.success(dictService.addDictData(code, dictData));
    }

    @PutMapping("/{code}/data/{id}")
    public Result<Boolean> updateData(@PathVariable String code, @PathVariable Long id,
                                      @RequestBody DictData dictData) {
        dictData.setId(id);
        return Result.success(dictService.updateDictData(code, dictData));
    }

    @DeleteMapping("/{code}/data/{id}")
    public Result<Boolean> deleteData(@PathVariable String code, @PathVariable Long id) {
        return Result.success(dictService.deleteDictData(code, id));
    }
}
