package com.knowledge.base.file.storage;

import java.io.InputStream;
import java.io.OutputStream;

public interface FileStorage {

    boolean upload(InputStream inputStream, String relativePath, long fileSize);

    long download(String relativePath, OutputStream outputStream);

    InputStream getInputStream(String relativePath);

    boolean delete(String relativePath);

    boolean exists(String relativePath);

    long getFileSize(String relativePath);

    String getStorageType();
}
