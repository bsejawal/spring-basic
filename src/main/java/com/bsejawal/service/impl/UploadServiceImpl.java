package com.bsejawal.service.impl;

import com.bsejawal.config.S3Properties;
import com.bsejawal.dto.UploadResponse;
import com.bsejawal.exception.FileUploadException;
import com.bsejawal.service.UploadService;
import com.bsejawal.utils.S3KeyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.PartEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Reactive S3 upload implementation. Streams a {@link PartEvent} flux straight
 * to a temp file via {@link DataBufferUtils#write}, then uploads the file to S3
 * with {@link S3AsyncClient} (which auto-splits large objects into parallel
 * multipart parts).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private final S3AsyncClient s3;
    private final S3Properties props;

    @Override
    public Mono<UploadResponse> uploadFile(String filename, MediaType contentType,
                                           Flux<PartEvent> partEvents) {
        if (partEvents == null) {
            return Mono.error(new IllegalArgumentException("File is required"));
        }

        String safeFilename = (filename == null) ? "" : filename;
        String key = S3KeyUtils.buildKey(props.getFolder(), safeFilename);
        String contentTypeStr = contentType != null ? contentType.toString() : null;

        return Mono.fromCallable(() -> Files.createTempFile("s3-upload-", ".tmp"))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(temp -> spool(partEvents, temp).thenReturn(temp))
                .flatMap(temp -> putToS3(temp, key, safeFilename, contentTypeStr)
                        .doFinally(signal -> deleteQuietly(temp)))
                .onErrorMap(this::wrap);
    }

    /**
     * Pipe each {@link PartEvent}'s {@link DataBuffer} into the temp file.
     * {@link DataBufferUtils#write} releases the buffers as it consumes them.
     */
    private Mono<Void> spool(Flux<PartEvent> events, Path destination) {
        Flux<DataBuffer> content = events.map(PartEvent::content);
        return DataBufferUtils.write(content, destination,
                StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private Mono<UploadResponse> putToS3(Path temp, String key,
                                         String originalName, String contentType) {
        return Mono.fromCallable(() -> Files.size(temp))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(size -> {
                    log.info("Starting upload: {} ({} bytes) -> s3://{}/{}",
                            originalName, size, props.getBucket(), key);

                    PutObjectRequest request = PutObjectRequest.builder()
                            .bucket(props.getBucket())
                            .key(key)
                            .contentType(contentType)
                            .build();

                    return Mono.fromFuture(() ->
                                    s3.putObject(request, AsyncRequestBody.fromFile(temp)))
                            .map(resp -> buildResponse(originalName, contentType, size, key));
                });
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

    private Throwable wrap(Throwable ex) {
        if (ex instanceof FileUploadException || ex instanceof IllegalArgumentException) {
            return ex;
        }
        String detail = ex.getMessage() != null && !ex.getMessage().isBlank()
                ? ": " + ex.getMessage()
                : "";
        return new FileUploadException("Failed to upload file to S3" + detail, ex);
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Failed to delete temp file {}", path, e);
        }
    }
}
