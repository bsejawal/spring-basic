package com.bsejawal.service;

import com.bsejawal.dto.UploadResponse;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.PartEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Contract for uploading files to the configured object store.
 * Implementations must perform the upload non-blocking using the
 * streaming {@link PartEvent} API.
 */
public interface UploadService {

    /**
     * Upload the body of a file part to the backing store.
     *
     * @param filename     original filename, may be blank
     * @param contentType  declared content type, may be {@code null}
     * @param partEvents   stream of {@link PartEvent} instances belonging to
     *                     a single multipart "file" part — terminates after
     *                     the event with {@link PartEvent#isLast()} == true
     * @return a {@link Mono} that completes once the object has been written
     *
     * @throws IllegalArgumentException                       if partEvents is null
     * @throws com.bsejawal.exception.FileUploadException    if the upload fails
     */
    Mono<UploadResponse> uploadFile(String filename, MediaType contentType,
                                    Flux<PartEvent> partEvents);
}
