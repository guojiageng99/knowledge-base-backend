package com.knowledge.base.file.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MediaMetadata {
    private Integer duration;
    private String resolution;
    private Integer bitrate;
    private String videoCodec;
    private String audioCodec;
}
