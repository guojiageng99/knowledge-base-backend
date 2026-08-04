package com.knowledge.base.file.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TranscodeMessage implements Serializable {
    private Long fileId;
    private String targetFormat;
}
