package com.knowledge.base.foundation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knowledge.base.foundation.entity.OperationLog;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {

    @Delete("DELETE FROM kb_operation_log WHERE create_time < #{date}")
    int deleteBeforeDate(@Param("date") String date);
}
