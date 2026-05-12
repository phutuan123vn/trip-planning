package com.kms.tripplanning.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletResponse;

import com.kms.tripplanning.controller.ApiController;
import com.kms.tripplanning.dto.ApiResponse;

class ApiWrapperConfigTest {

    private ApiWrapperConfig config;
    private MethodParameter methodParameter;
    private ServerHttpRequest request;
    private ServerHttpResponse response;

    @BeforeEach
    void setUp() {
        config = new ApiWrapperConfig();
        methodParameter = mock(MethodParameter.class);
        request = mock(ServerHttpRequest.class);
        response = mock(ServerHttpResponse.class);
    }

    // ========== supports ==========

    @Test
    void supports_shouldReturnTrue_whenClassHasApiControllerAnnotation() {
        Class<?> mockClass = AnnotatedClass.class;
        when(methodParameter.getContainingClass()).thenReturn((Class) mockClass);

        boolean result = config.supports(methodParameter, MappingJackson2HttpMessageConverter.class);

        assertThat(result).isTrue();
    }

    @Test
    void supports_shouldReturnFalse_whenClassNotAnnotated() {
        Class<?> mockClass = NonAnnotatedClass.class;
        when(methodParameter.getContainingClass()).thenReturn((Class) mockClass);

        boolean result = config.supports(methodParameter, MappingJackson2HttpMessageConverter.class);

        assertThat(result).isFalse();
    }

    // ========== beforeBodyWrite ==========

    @Test
    void beforeBodyWrite_shouldReturnBodyUnchanged_whenNotJson() {
        Object body = "String response";
        Object result = config.beforeBodyWrite(
                body, methodParameter, MediaType.TEXT_PLAIN,
                MappingJackson2HttpMessageConverter.class, request, response);

        assertThat(result).isEqualTo(body);
    }

    @Test
    void beforeBodyWrite_shouldReturnNull_when204Status() {
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        mockResponse.setStatus(HttpStatus.NO_CONTENT.value());
        ServletServerHttpResponse servletResponse = new ServletServerHttpResponse(mockResponse);

        Object result = config.beforeBodyWrite(
                "Any Body", methodParameter, MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class, request, servletResponse);

        assertThat(result).isNull();
    }

    @Test
    void beforeBodyWrite_shouldNotRewrap_alreadyWrappedResponse() {
        ApiResponse.SuccessResponse<String> wrappedBody = ApiResponse.success("Hello");

        Object result = config.beforeBodyWrite(
                wrappedBody, methodParameter, MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class, request, response);

        assertThat(result).isSameAs(wrappedBody);
    }

    @Test
    @SuppressWarnings("unchecked")
    void beforeBodyWrite_shouldWrapPage() {
        Page<String> page = new PageImpl<>(List.of("A", "B"), PageRequest.of(0, 10), 2);

        Object result = config.beforeBodyWrite(
                page, methodParameter, MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class, request, response);

        assertThat(result).isInstanceOf(ApiResponse.SuccessResponse.class);
        ApiResponse.SuccessResponse<String> wrapped = (ApiResponse.SuccessResponse<String>) result;
        assertThat(wrapped.getData()).containsExactly("A", "B");
        assertThat(wrapped.getPagination()).isNotNull();
        assertThat(wrapped.getPagination().getTotalElements()).isEqualTo(2);
    }

    @Test
    @SuppressWarnings("unchecked")
    void beforeBodyWrite_shouldWrapList() {
        List<String> list = List.of("A", "B");

        Object result = config.beforeBodyWrite(
                list, methodParameter, MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class, request, response);

        assertThat(result).isInstanceOf(ApiResponse.SuccessResponse.class);
        ApiResponse.SuccessResponse<String> wrapped = (ApiResponse.SuccessResponse<String>) result;
        assertThat(wrapped.getData()).containsExactly("A", "B");
    }

    @Test
    @SuppressWarnings("unchecked")
    void beforeBodyWrite_shouldWrapNullAsEmptyList() {
        Object result = config.beforeBodyWrite(
                null, methodParameter, MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class, request, response);

        assertThat(result).isInstanceOf(ApiResponse.SuccessResponse.class);
        ApiResponse.SuccessResponse<Object> wrapped = (ApiResponse.SuccessResponse<Object>) result;
        assertThat(wrapped.getData()).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void beforeBodyWrite_shouldWrapSingleObject() {
        Object body = "Single Item";

        Object result = config.beforeBodyWrite(
                body, methodParameter, MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class, request, response);

        assertThat(result).isInstanceOf(ApiResponse.SuccessResponse.class);
        ApiResponse.SuccessResponse<String> wrapped = (ApiResponse.SuccessResponse<String>) result;
        assertThat(wrapped.getData()).containsExactly("Single Item");
    }

    // Dummy classes for supports test
    @ApiController
    private static class AnnotatedClass {
    }

    private static class NonAnnotatedClass {
    }
}
