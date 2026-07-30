package com.knowledge.base.foundation.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.knowledge.base.foundation.entity.Dict;
import com.knowledge.base.foundation.entity.DictData;

import java.util.List;

public interface DictService {

    IPage<Dict> pageDicts(Long current, Long size, String keyword);

    Dict getDictByCode(String code);

    boolean createDict(Dict dict);

    boolean updateDict(String code, Dict dict);

    boolean deleteDict(String code);

    List<DictData> getDictData(String code);

    boolean addDictData(String code, DictData dictData);

    boolean updateDictData(String code, DictData dictData);

    boolean deleteDictData(String code, Long id);
}
