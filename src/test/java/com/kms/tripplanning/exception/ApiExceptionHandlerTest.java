package com.kms.tripplanning.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.kms.tripplanning.dto.ApiResponse.ErrorResponse;
import com.kms.tripplanning.dto.ApiResponse.ValidationError;
import com.kms.tripplanning.exception.types.NotFoundException;
import com.kms.tripplanning.exception.types.ValidationException;

class ApiExceptionHandlerTest {

    private ApiExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ApiExceptionHandler();
    }

    @Test
    void handleException_shouldReturn500_withErrorResponse() {
        Exception ex = new Exception("Something went wrong");
        ResponseEntity<ErrorResponse<String>> response = handler.handleException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().getErrorCode()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(response.getBody().getDetails()).isEqualTo("Something went wrong");
    }

    @Test
    void handleException_shouldIncludeExceptionMessage() {
        Exception ex = new Exception("Database connection failed");
        ResponseEntity<ErrorResponse<String>> response = handler.handleException(ex);

        assertThat(response.getBody().getDetails()).isEqualTo("Database connection failed");
    }

    @Test
    void handleValidationException_shouldReturn400_withErrorResponse() {
        ValidationException ex = new ValidationException("Invalid field");
        ResponseEntity<ErrorResponse<String>> response = handler.handleValidationException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Validation Error");
        assertThat(response.getBody().getErrorCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().getDetails()).isEqualTo("Invalid field");
    }

    @Test
    void handleMethodArgumentNotValidException_shouldReturn400_withFieldErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError = new FieldError("objectName", "fieldName", "must not be empty");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ErrorResponse<List<ValidationError>>> response = handler.handleMethodArgumentNotValidException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Validation Error");
        assertThat(response.getBody().getErrorCode()).isEqualTo("VALIDATION_ERROR");
        
        List<ValidationError> errors = response.getBody().getDetails();
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).getField()).isEqualTo("fieldName");
        assertThat(errors.get(0).getMessage()).isEqualTo("must not be empty");
    }

    @Test
    void handleNotFoundException_shouldReturn404_withErrorResponse() {
        NotFoundException ex = new NotFoundException("User not found");
        ResponseEntity<ErrorResponse<String>> response = handler.handleNotFoundException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Not Found");
        assertThat(response.getBody().getErrorCode()).isEqualTo("NOT_FOUND");
        assertThat(response.getBody().getDetails()).isEqualTo("User not found");
    }
}
