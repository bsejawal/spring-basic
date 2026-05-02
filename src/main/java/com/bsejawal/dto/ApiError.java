package com.bsejawal.dto;

import lombok.Builder;

import java.time.Instant;

/**
 * Standard error response envelope returned by {@link com.bsejawal.exception.GlobalExceptionHandler}.
 */
@Builder
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {}
