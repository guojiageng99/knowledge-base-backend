package com.knowledge.base.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knowledge.base.document.entity.DocumentVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DocumentVersionMapper extends BaseMapper<DocumentVersion> {

    List<DocumentVersion> selectByDocumentId(@Param("documentId") Long documentId);
    DocumentVersion selectByDocumentIdAndVersion(@Param("documentId") Long documentId, @Param("version") Integer version);
    DocumentVersion selectLatestByDocumentId(@Param("documentId") Long documentId);
    DocumentVersion selectFirstByDocumentId(@Param("documentId") Long documentId);
    Integer getNextVersionNumber(@Param("documentId") Long documentId);
    Long countByDocumentId(@Param("documentId") Long documentId);
    List<DocumentVersion> selectByOperatorId(@Param("operatorId") Long operatorId);
    List<DocumentVersion> compareVersions(@Param("documentId") Long documentId, @Param("version1") Integer version1, @Param("version2") Integer version2);
}
