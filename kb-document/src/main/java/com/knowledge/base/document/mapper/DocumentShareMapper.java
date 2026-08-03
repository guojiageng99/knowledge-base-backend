package com.knowledge.base.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knowledge.base.document.entity.DocumentShare;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface DocumentShareMapper extends BaseMapper<DocumentShare> {
    @Select("SELECT * FROM kb_document_share WHERE share_id = #{shareId} AND deleted = 0")
    DocumentShare selectByShareId(@Param("shareId") String shareId);

    @Select("SELECT * FROM kb_document_share WHERE document_id = #{documentId} AND status = 0 AND deleted = 0 ORDER BY create_time DESC")
    List<DocumentShare> selectValidSharesByDocumentId(@Param("documentId") Long documentId);

    @Select("SELECT * FROM kb_document_share WHERE sharer_id = #{sharerId} AND deleted = 0 ORDER BY create_time DESC")
    List<DocumentShare> selectBySharerId(@Param("sharerId") Long sharerId);

    @Update("UPDATE kb_document_share SET access_count = access_count + 1 WHERE share_id = #{shareId} AND status = 0 AND deleted = 0 AND (access_limit = 0 OR access_count < access_limit)")
    int incrementAccessCount(@Param("shareId") String shareId);
}
