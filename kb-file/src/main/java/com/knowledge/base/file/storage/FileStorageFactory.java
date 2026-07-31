package com.knowledge.base.file.storage;

import com.knowledge.base.common.exception.BusinessException;
import com.knowledge.base.file.config.FileStorageProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileStorageFactory {

    private final ApplicationContext applicationContext;
    private final FileStorageProperties properties;
    private FileStorage storage;

    @PostConstruct
    void initialize() {
        StorageType type = StorageType.fromCode(properties.getType());
        storage = applicationContext.getBean(type.getBeanName(), FileStorage.class);
    }

    public FileStorage getStorage() {
        if (storage == null) {
            throw new BusinessException("File storage is not initialized");
        }
        return storage;
    }
}
