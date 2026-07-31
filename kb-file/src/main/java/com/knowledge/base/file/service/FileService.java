package com.knowledge.base.file.service;

import com.knowledge.base.common.result.PageResult;
import com.knowledge.base.file.dto.FileQueryDTO;
import com.knowledge.base.file.dto.FileUploadDTO;
import com.knowledge.base.file.vo.FileInfoVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileService {

    FileInfoVO uploadFile(MultipartFile file, FileUploadDTO dto);

    List<FileInfoVO> uploadFiles(MultipartFile[] files, FileUploadDTO dto);

    void downloadFile(Long fileId, HttpServletResponse response) throws IOException;

    void previewFile(Long fileId, HttpServletResponse response) throws IOException;

    FileInfoVO getFileInfo(Long fileId);

    PageResult<FileInfoVO> pageFiles(FileQueryDTO dto);

    boolean deleteFile(Long fileId);

    boolean batchDeleteFiles(List<Long> fileIds);
}
