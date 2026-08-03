package com.knowledge.base.document.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.knowledge.base.document.dto.DocumentDTO;
import com.knowledge.base.document.entity.Document;
import com.knowledge.base.document.vo.DocumentVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService extends IService<Document> {

    Long createDocument(DocumentDTO documentDTO);
    Boolean updateDocument(DocumentDTO documentDTO);
    Boolean deleteDocument(Long documentId);
    DocumentVO getDocumentById(Long documentId);
    DocumentVO viewDocument(Long documentId);
    IPage<DocumentVO> pageDocuments(Long current, Long size, Long categoryId, String keyword, Integer status);
    String uploadDocumentFile(MultipartFile file);
    Boolean likeDocument(Long documentId);
    Boolean favoriteDocument(Long documentId);
    Boolean publishDocument(Long documentId);
    Boolean archiveDocument(Long documentId);
    Boolean addTagsToDocument(Long documentId, List<Long> tagIds);
    Boolean updateDocumentContent(Long documentId, String content);
    String getDocumentContent(Long documentId);
    String uploadImageFromUrl(String imageUrl);
}
