package com.knowledge.base.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knowledge.base.file.entity.FileInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FileMapper extends BaseMapper<FileInfo> {

    List<FileInfo> selectByUploaderId(@Param("uploaderId") Long uploaderId);

    List<FileInfo> selectByFileType(@Param("fileType") String fileType);

    FileInfo selectByFileHash(@Param("fileHash") String fileHash);

    List<FileInfo> selectByStorageType(@Param("storageType") String storageType);

    List<FileInfo> selectByAccessLevel(@Param("accessLevel") Integer accessLevel);

    int incrementDownloadCount(@Param("fileId") Long fileId);

    int updateStatus(@Param("fileId") Long fileId, @Param("status") Integer status);

    boolean checkFileHashExists(@Param("fileHash") String fileHash);

    long countByUploaderId(@Param("uploaderId") Long uploaderId);

    long sumFileSizeByUploaderId(@Param("uploaderId") Long uploaderId);

    List<FileInfo> selectByBucketName(@Param("bucketName") String bucketName);
}
