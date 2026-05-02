package com.bsejawal.controller;

import com.bsejawal.dto.UploadResponse;
import com.bsejawal.service.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

/**
 * REST endpoints for file uploads. Returns a {@link CompletableFuture} so the
 * request thread is released back to Tomcat while the upload is in flight.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final UploadService uploadService;

    @PostMapping(path = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<UploadResponse>> upload(
            @RequestParam("file") MultipartFile file) {

        log.debug("Received upload request: name={}, size={}",
                file != null ? file.getOriginalFilename() : null,
                file != null ? file.getSize() : 0);

        return uploadService.uploadFile(file)
                .thenApply(ResponseEntity::ok);
    }
}
