package com.knowledge.base.file.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.knowledge.base.common.config.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_file")
public class FileInfo extends BaseEntity {

    private String originalName;
    private String storedName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private String mimeType;
    private String fileHash;
    private String storageType;
    private String bucketName;
    private Long uploaderId;
    private Integer accessLevel;
    private Integer downloadCount;
    private Integer status;
    private Integer duration;
    private String resolution;
    private Integer bitrate;
    private String transcodeStatus;
    private String hlsPath;
    private String thumbnailPath;
}
