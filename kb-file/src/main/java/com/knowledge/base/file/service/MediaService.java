package com.knowledge.base.file.service;

import com.knowledge.base.file.vo.MediaMetadata;

public interface MediaService {
    MediaMetadata probeMediaInfo(Long fileId);
    String transcodeToHls(Long fileId);
    String generateThumbnail(Long fileId);
    void updateTranscodeStatus(Long fileId, String status);
}
