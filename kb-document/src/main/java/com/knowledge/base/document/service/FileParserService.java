package com.knowledge.base.document.service;

import org.springframework.web.multipart.MultipartFile;

/** Converts supported office and text files into Markdown for knowledge documents. */
public interface FileParserService {
    String parse(MultipartFile file) throws Exception;
    boolean isSupported(String extension);
}
