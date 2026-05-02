package com.bsejawal.exception;

import com.bsejawal.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.concurrent.CompletionException;

/**
 * Centralized exception handling for the REST API. Returns a uniform
 * {@link ApiError} body with a sensible HTTP status for every error.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ApiError> handleUpload(FileUploadException ex, HttpServletRequest req) {
        log.error("File upload error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), req);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleTooLarge(MaxUploadSizeExceededException ex,
                                                   HttpServletRequest req) {
        return build(HttpStatus.PAYLOAD_TOO_LARGE,
                "File exceeds the configured maximum size", req);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParam(MissingServletRequestParameterException ex,
                                                       HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST,
                "Missing required parameter: " + ex.getParameterName(), req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException ex,
                                                     HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NoResourceFoundException ex,
                                                   HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND,
                "No endpoint mapped for " + req.getMethod() + " " + req.getRequestURI(), req);
    }

    @ExceptionHandler(CompletionException.class)
    public ResponseEntity<ApiError> handleCompletion(CompletionException ex,
                                                     HttpServletRequest req) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        if (cause instanceof FileUploadException fue) {
            return handleUpload(fue, req);
        }
        log.error("Async pipeline failed", cause);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                cause.getMessage() != null ? cause.getMessage() : "Unexpected server error", req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Unexpected error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", req);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest req) {
        ApiError body = ApiError.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(req.getRequestURI())
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
