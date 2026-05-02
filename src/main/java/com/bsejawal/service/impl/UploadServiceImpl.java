package com.bsejawal.service.impl;

import com.bsejawal.config.S3Properties;
import com.bsejawal.dto.UploadResponse;
import com.bsejawal.exception.FileUploadException;
import com.bsejawal.service.UploadService;
import com.bsejawal.utils.S3KeyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Non-blocking S3 upload implementation. Spools the multipart file to a temp
 * file on disk and streams it to S3 via {@link S3AsyncClient}, which auto-splits
 * large uploads into parallel multipart parts.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private final S3AsyncClient s3;
    private final S3Properties props;

    @Override
    public CompletableFuture<UploadResponse> uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required and must not be empty");
        }

        String originalName = file.getOriginalFilename();
        String contentType = file.getContentType();
        long size = file.getSize();
        String key = S3KeyUtils.buildKey(props.getFolder(), originalName);

        Path temp = spoolToTempFile(file);

        log.info("Starting upload: {} ({} bytes) -> s3://{}/{}",
                originalName, size, props.getBucket(), key);

        return s3.putObject(
                        PutObjectRequest.builder()
                                .bucket(props.getBucket())
                                .key(key)
                                .contentType(contentType)
                                .build(),
                        AsyncRequestBody.fromFile(temp))
                .thenApply(resp -> buildResponse(originalName, contentType, size, key))
                .exceptionallyCompose(ex -> {
                    Throwable cause = (ex instanceof CompletionException && ex.getCause() != null)
                            ? ex.getCause() : ex;
                    return CompletableFuture.failedFuture(
                            new FileUploadException(
                                    "Failed to upload " + originalName + " to S3", cause));
                })
                .whenComplete((r, ex) -> deleteQuietly(temp));
    }

    private UploadResponse buildResponse(String originalName, String contentType,
                                         long size, String key) {
        String location = "s3://" + props.getBucket() + "/" + key;
        log.info("Upload complete: {}", location);
        return UploadResponse.builder()
                .status("SUCCESS")
                .message("File uploaded successfully")
                .bucket(props.getBucket())
                .key(key)
                .location(location)
                .originalFilename(originalName)
                .size(size)
                .contentType(contentType)
                .build();
    }

    private Path spoolToTempFile(MultipartFile file) {
        try {
            Path temp = Files.createTempFile("s3-upload-", ".tmp");
            file.transferTo(temp);
            return temp;
        } catch (IOException e) {
            throw new FileUploadException("Failed to spool upload to temp file", e);
        }
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Failed to delete temp file {}", path, e);
        }
    }
}
