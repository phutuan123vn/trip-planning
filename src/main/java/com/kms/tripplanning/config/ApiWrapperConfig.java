package com.kms.tripplanning.config;

import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.kms.tripplanning.controller.ApiController;
import com.kms.tripplanning.dto.ApiResponse;


@RestControllerAdvice
public class ApiWrapperConfig implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        boolean isApiControllerAnnotated = returnType.getContainingClass().isAnnotationPresent(ApiController.class);
        return isApiControllerAnnotated;
    }

    @Override
    public @Nullable Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType,
            org.springframework.http.MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
            ServerHttpResponse response) {
        if (!MediaType.APPLICATION_JSON.isCompatibleWith(selectedContentType)) {
            return body;
        }

        // Skip 204
        if (response instanceof ServletServerHttpResponse servletResponse) {
            if (servletResponse.getServletResponse().getStatus() == 204) {
                return null;
            }
        }

        // Already wrapped
        if (body instanceof ApiResponse.SuccessResponse) {
            return body;
        }

        // Page
        if (body instanceof org.springframework.data.domain.Page<?> page) {
            return ApiResponse.success(page);
        }

        // List
        if (body instanceof java.util.List<?> list) {
            return ApiResponse.success(list);
        }

        // Null
        if (body == null) {
            return ApiResponse.success(java.util.List.of());
        }

        return ApiResponse.success(body);
        
    }

}
