package com.bsejawal.service;

import com.bsejawal.dto.UploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

/**
 * Contract for uploading files to the configured object store.
 * Implementations must perform the upload non-blocking.
 */
public interface UploadService {

    /**
     * Upload the given multipart file. The returned future completes once the
     * object has been written to the backing store.
     *
     * @throws IllegalArgumentException     if the file is null or empty
     * @throws com.bsejawal.exception.FileUploadException if the upload fails
     */
    CompletableFuture<UploadResponse> uploadFile(MultipartFile file);
}
