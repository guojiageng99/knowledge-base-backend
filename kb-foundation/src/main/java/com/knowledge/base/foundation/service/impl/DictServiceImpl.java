package com.knowledge.base.foundation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.knowledge.base.common.exception.BusinessException;
import com.knowledge.base.common.utils.SnowflakeIdGenerator;
import com.knowledge.base.foundation.entity.Dict;
import com.knowledge.base.foundation.entity.DictData;
import com.knowledge.base.foundation.mapper.DictDataMapper;
import com.knowledge.base.foundation.mapper.DictMapper;
import com.knowledge.base.foundation.service.DictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DictServiceImpl implements DictService {

    private final DictMapper dictMapper;
    private final DictDataMapper dictDataMapper;

    @Override
    public IPage<Dict> pageDicts(Long current, Long size, String keyword) {
        LambdaQueryWrapper<Dict> query = new LambdaQueryWrapper<Dict>()
                .orderByAsc(Dict::getSort);
        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper.like(Dict::getDictCode, keyword)
                    .or().like(Dict::getDictName, keyword));
        }
        return dictMapper.selectPage(new Page<>(current, size), query);
    }

    @Override
    public Dict getDictByCode(String code) {
        requireCode(code);
        return dictMapper.selectByDictCode(code);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createDict(Dict dict) {
        if (!StringUtils.hasText(dict.getDictCode()) || !StringUtils.hasText(dict.getDictName())) {
            throw new BusinessException("Dictionary code and name are required");
        }
        if (dictMapper.selectByDictCode(dict.getDictCode()) != null) {
            throw new BusinessException("Dictionary code already exists");
        }
        dict.setId(SnowflakeIdGenerator.nextId());
        return dictMapper.insert(dict) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDict(String code, Dict dict) {
        Dict existing = requireDict(code);
        existing.setDictName(dict.getDictName());
        existing.setDictType(dict.getDictType());
        existing.setDescription(dict.getDescription());
        existing.setSort(dict.getSort());
        existing.setStatus(dict.getStatus());
        return dictMapper.updateById(existing) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDict(String code) {
        Dict existing = requireDict(code);
        dictDataMapper.delete(new LambdaQueryWrapper<DictData>().eq(DictData::getDictId, existing.getId()));
        return dictMapper.deleteById(existing.getId()) > 0;
    }

    @Override
    public List<DictData> getDictData(String code) {
        requireCode(code);
        return dictDataMapper.selectByDictCode(code);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addDictData(String code, DictData dictData) {
        Dict dict = requireDict(code);
        dictData.setId(SnowflakeIdGenerator.nextId());
        dictData.setDictId(dict.getId());
        dictData.setDictCode(dict.getDictCode());
        dictData.setCreateTime(LocalDateTime.now());
        return dictDataMapper.insert(dictData) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDictData(String code, DictData dictData) {
        Dict dict = requireDict(code);
        DictData existing = dictDataMapper.selectById(dictData.getId());
        if (existing == null || !dict.getId().equals(existing.getDictId())) {
            throw new BusinessException("Dictionary data does not exist");
        }
        dictData.setDictId(dict.getId());
        dictData.setDictCode(dict.getDictCode());
        return dictDataMapper.updateById(dictData) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDictData(String code, Long id) {
        Dict dict = requireDict(code);
        DictData existing = dictDataMapper.selectById(id);
        if (existing == null || !dict.getId().equals(existing.getDictId())) {
            throw new BusinessException("Dictionary data does not exist");
        }
        return dictDataMapper.deleteById(id) > 0;
    }

    private Dict requireDict(String code) {
        requireCode(code);
        Dict dict = dictMapper.selectByDictCode(code);
        if (dict == null) {
            throw new BusinessException("Dictionary does not exist");
        }
        return dict;
    }

    private void requireCode(String code) {
        if (!StringUtils.hasText(code)) {
            throw new BusinessException("Dictionary code is required");
        }
    }
}
