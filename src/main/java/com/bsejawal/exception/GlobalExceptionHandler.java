package com.bsejawal.exception;

import com.bsejawal.dto.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

/**
 * Centralized exception handling for the reactive REST API. Returns a uniform
 * {@link ApiError} body with a sensible HTTP status for every error.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ApiError> handleUpload(FileUploadException ex, ServerWebExchange exchange) {
        log.error("File upload error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), exchange);
    }

    @ExceptionHandler(DataBufferLimitException.class)
    public ResponseEntity<ApiError> handleBufferLimit(DataBufferLimitException ex,
                                                      ServerWebExchange exchange) {
        return build(HttpStatus.PAYLOAD_TOO_LARGE,
                "Request body exceeds the configured buffer limit", exchange);
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ApiError> handleBadInput(ServerWebInputException ex,
                                                   ServerWebExchange exchange) {
        String message = ex.getReason() != null ? ex.getReason() : "Invalid request";
        return build(HttpStatus.BAD_REQUEST, message, exchange);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArg(IllegalArgumentException ex,
                                                     ServerWebExchange exchange) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), exchange);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException ex,
                                                         ServerWebExchange exchange) {
        HttpStatusCode code = ex.getStatusCode();
        HttpStatus status = HttpStatus.resolve(code.value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        return build(status, message, exchange);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, ServerWebExchange exchange) {
        log.error("Unexpected error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", exchange);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message,
                                           ServerWebExchange exchange) {
        ApiError body = ApiError.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(exchange.getRequest().getPath().value())
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
