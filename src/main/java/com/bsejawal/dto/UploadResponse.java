package com.bsejawal.dto;

import lombok.Builder;

/**
 * Response payload for a successful file upload.
 */
@Builder
public record UploadResponse(
        String status,
        String message,
        String bucket,
        String key,
        String location,
        String originalFilename,
        long size,
        String contentType
) {}
