package com.kms.tripplanning.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.kms.tripplanning.dto.ApiResponse.ErrorResponse;
import com.kms.tripplanning.dto.ApiResponse.PaginationResponse;
import com.kms.tripplanning.dto.ApiResponse.SuccessResponse;

class ApiResponseTest {

    @Test
    void success_withList_shouldWrapDataWithDefaultPagination() {
        List<String> data = List.of("A", "B");
        SuccessResponse<String> response = ApiResponse.success(data);

        assertThat(response.getData()).containsExactly("A", "B");
        assertThat(response.getPagination()).isNotNull();
        assertThat(response.getPagination().getTotalElements()).isEqualTo(2);
        assertThat(response.getPagination().getSize()).isEqualTo(2);
        assertThat(response.getPagination().getPage()).isEqualTo(1);
    }

    @Test
    void success_withEmptyList_shouldReturnEmptyDataWithZeroPagination() {
        List<String> data = Collections.emptyList();
        SuccessResponse<String> response = ApiResponse.success(data);

        assertThat(response.getData()).isEmpty();
        assertThat(response.getPagination()).isNotNull();
        assertThat(response.getPagination().getTotalElements()).isZero();
        assertThat(response.getPagination().getSize()).isZero();
        assertThat(response.getPagination().getPage()).isEqualTo(1);
    }

    @Test
    void success_withListAndPagination_shouldUseProvidedPagination() {
        List<String> data = List.of("A");
        PaginationResponse pagination = PaginationResponse.builder()
                .page(2)
                .size(10)
                .totalElements(15)
                .totalPages(2)
                .build();

        SuccessResponse<String> response = ApiResponse.success(data, pagination);

        assertThat(response.getData()).containsExactly("A");
        assertThat(response.getPagination()).isSameAs(pagination);
    }

    @Test
    void success_withSingleObject_shouldWrapInList() {
        String data = "Single";
        SuccessResponse<String> response = ApiResponse.success(data);

        assertThat(response.getData()).containsExactly("Single");
        assertThat(response.getPagination()).isNotNull();
        assertThat(response.getPagination().getTotalElements()).isEqualTo(1);
    }

    @Test
    void success_withPage_shouldExtractContentAndPagination() {
        Page<String> page = new PageImpl<>(List.of("A", "B"), PageRequest.of(1, 10), 12);
        
        SuccessResponse<String> response = ApiResponse.success(page);

        assertThat(response.getData()).containsExactly("A", "B");
        assertThat(response.getPagination().getPage()).isEqualTo(2); // PageRequest is 0-indexed, response is 1-indexed
        assertThat(response.getPagination().getSize()).isEqualTo(10);
        assertThat(response.getPagination().getTotalElements()).isEqualTo(12);
        assertThat(response.getPagination().getTotalPages()).isEqualTo(2);
        assertThat(response.getPagination().isHasNext()).isFalse();
        assertThat(response.getPagination().isHasPrevious()).isTrue();
    }

    @Test
    void error_shouldBuildErrorResponse() {
        ErrorResponse<String> response = ApiResponse.error("Not Found", "NOT_FOUND", "User 123");

        assertThat(response.getMessage()).isEqualTo("Not Found");
        assertThat(response.getErrorCode()).isEqualTo("NOT_FOUND");
        assertThat(response.getDetails()).isEqualTo("User 123");
    }
}
