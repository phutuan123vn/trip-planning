package com.kms.tripplanning.exception;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.kms.tripplanning.dto.ApiResponse;
import com.kms.tripplanning.dto.ApiResponse.ErrorResponse;
import com.kms.tripplanning.dto.ApiResponse.ValidationError;
import com.kms.tripplanning.exception.types.NotFoundException;
import com.kms.tripplanning.exception.types.ValidationException;

import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@RequiredArgsConstructor
public class ApiExceptionHandler {

        Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse<String>> handleException(Exception e) {
                var errorResponse = ApiResponse.<String>error(
                                "Internal Server Error",
                                "INTERNAL_SERVER_ERROR",
                                e.getMessage());
                e.printStackTrace();
                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @ExceptionHandler(ValidationException.class)
        public ResponseEntity<ErrorResponse<String>> handleValidationException(ValidationException e) {
                var errorResponse = ApiResponse.<String>error(
                                "Validation Error",
                                "VALIDATION_ERROR",
                                e.getMessage());
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse<List<ValidationError>>> handleMethodArgumentNotValidException(
                        MethodArgumentNotValidException e) {
                var listErrors = e.getBindingResult().getFieldErrors()
                                .stream()
                                .map(error -> new ValidationError(error.getField(), error.getDefaultMessage(),
                                                error.getCode()))
                                .toList();
                var errorResponse = ApiResponse.<List<ValidationError>>error(
                                "Validation Error",
                                "VALIDATION_ERROR",
                                listErrors);
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(NotFoundException.class)
        public ResponseEntity<ErrorResponse<String>> handleNotFoundException(NotFoundException e) {
                var errorResponse = ApiResponse.<String>error(
                                "Not Found",
                                "NOT_FOUND",
                                e.getMessage());
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
}
