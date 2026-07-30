package com.knowledge.base.common.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long current;
    private Long size;
    private Long total;
    private Long pages;
    private List<T> records;

    public static <T> PageResult<T> of(long current, long size, long total, List<T> records) {
        long pages = size == 0 ? 0 : (total + size - 1) / size;
        return new PageResult<>(current, size, total, pages, records);
    }

    public static <T> PageResult<T> empty(long current, long size) {
        return of(current, size, 0, Collections.emptyList());
    }
}
