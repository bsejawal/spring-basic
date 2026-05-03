package com.bsejawal.controller;

import com.bsejawal.dto.UploadResponse;
import com.bsejawal.service.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePartEvent;
import org.springframework.http.codec.multipart.PartEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive REST endpoints for file uploads. Consumes the request body as a
 * stream of {@link PartEvent} so file content is never fully buffered in memory
 * at the framework level — events flow straight through to S3 via the service.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    /** Form field name expected to carry the file. */
    private static final String FILE_PART_NAME = "file";

    private final UploadService uploadService;

    @PostMapping(path = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<UploadResponse>> upload(@RequestBody Flux<PartEvent> events) {
        // Each window contains all events for a single part, terminating with isLast() == true.
        return events.windowUntil(PartEvent::isLast)
                .concatMap(this::handlePart)
                .next()
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "Missing required multipart part: " + FILE_PART_NAME)))
                .map(ResponseEntity::ok);
    }

    private Publisher<UploadResponse> handlePart(Flux<PartEvent> partWindow) {
        return partWindow.switchOnFirst((signal, parts) -> {
            if (!signal.hasValue()) {
                return Flux.<UploadResponse>empty();
            }
            PartEvent first = signal.get();
            if (first instanceof FilePartEvent fpe && FILE_PART_NAME.equals(first.name())) {
                String filename = fpe.filename();
                MediaType contentType = fpe.headers().getContentType();
                log.debug("Received upload: name={}, contentType={}", filename, contentType);
                return uploadService.uploadFile(filename, contentType, parts);
            }
            // Drain non-matching parts so their data buffers are released.
            return parts.thenMany(Flux.<UploadResponse>empty());
        });
    }
}
