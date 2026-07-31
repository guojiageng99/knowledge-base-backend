package com.knowledge.base.file.storage;

public enum StorageType {
    RUSTFS("rustfs", "rustFileStorage");

    private final String code;
    private final String beanName;

    StorageType(String code, String beanName) {
        this.code = code;
        this.beanName = beanName;
    }

    public String getBeanName() {
        return beanName;
    }

    public static StorageType fromCode(String code) {
        for (StorageType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported storage type: " + code);
    }
}
