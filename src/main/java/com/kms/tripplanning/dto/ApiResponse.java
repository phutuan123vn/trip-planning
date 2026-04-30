package com.kms.tripplanning.dto;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;

import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

public class ApiResponse {

    @Data
    @AllArgsConstructor
    public static class ValidationError {
        private String field;
        private String message;
        private String code;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class ErrorResponse<T> {
        private String message;
        private String errorCode;
        private T details;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class PaginationResponse {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean hasNext;
        private boolean hasPrevious;

    }

    @Data
    public static class SuccessResponse<T> {

        @Nonnull
        private List<T> data;

        private PaginationResponse pagination;

        public SuccessResponse(
                List<T> data,
                PaginationResponse pagination) {
            this.data = data;
            this.pagination = pagination;
        }

        public SuccessResponse(List<T> data) {
            this.data = data;
            if (data.isEmpty()) {
                this.pagination = PaginationResponse.builder()
                        .page(1)
                        .size(0)
                        .totalElements(0)
                        .totalPages(1)
                        .hasNext(false)
                        .hasPrevious(false)
                        .build();
            } else {
                this.pagination = PaginationResponse.builder()
                        .page(1)
                        .size(data.size())
                        .totalElements(data.size())
                        .totalPages(1)
                        .hasNext(false)
                        .hasPrevious(false)
                        .build();
            }
        }
    }

    public static <T> SuccessResponse<T> success(List<T> data, @Nullable PaginationResponse pagination) {
        if (pagination != null) {
            return new SuccessResponse<T>(data, pagination);
        }
        return new SuccessResponse<T>(data);
    }

    public static <T> ErrorResponse<T> error(String message, String errorCode, T details) {
        return ErrorResponse.<T>builder()
                .message(message)
                .errorCode(errorCode)
                .details(details)
                .build();
    }

    public static <T> SuccessResponse<T> success(List<T> data) {
        return success(data, null);
    }

    public static <T> SuccessResponse<T> success(T data) {
        return success(List.of(data), null);
    }

    public static <T> SuccessResponse<T> success(Page<T> page) {
        return success(page.getContent(), PaginationResponse.builder()
                .page(page.getNumber() + 1)
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build());
    }

}
