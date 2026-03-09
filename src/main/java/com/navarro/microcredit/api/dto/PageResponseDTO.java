package com.navarro.microcredit.api.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public record PageResponseDTO<T>(
        List<T> data,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <T, R> PageResponseDTO<R> fromPage(Page<T> page, Function<T, R> mapper) {
        List<R> data = page.stream().map(mapper).toList();

        return new PageResponseDTO<>(
                data,
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
