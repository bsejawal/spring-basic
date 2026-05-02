package com.bsejawal.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
public class FileController {

    /** Files larger than this threshold use S3 multipart upload. */
    private static final long MULTIPART_THRESHOLD = 10L * 1024 * 1024; // 10 MB
    /** Size of each part in a multipart upload. S3 minimum is 5 MB (last part can be smaller). */
    private static final long PART_SIZE = 10L * 1024 * 1024; // 10 MB

    @Value("${aws.s3.endpoint}")
    private String endpoint;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.access-key}")
    private String accessKey;

    @Value("${aws.s3.secret-key}")
    private String secretKey;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.folder:}")
    private String folder;

    @PostMapping(path = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public String upload(@RequestParam(required = false) MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "No file provided or file is empty";
        }

        try (S3Client s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build()) {

            // Make sure the bucket exists (handy for first run against LocalStack)
            try {
                s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
            } catch (NoSuchBucketException e) {
                log.info("Bucket {} not found, creating it", bucket);
                s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
            }

            String originalName = file.getOriginalFilename();
            String fileName = (originalName == null || originalName.isBlank())
                    ? UUID.randomUUID().toString()
                    : UUID.randomUUID() + "-" + originalName;
            String prefix = (folder == null || folder.isBlank())
                    ? ""
                    : (folder.endsWith("/") ? folder : folder + "/");
            String key = prefix + fileName;

            long size = file.getSize();
            if (size <= MULTIPART_THRESHOLD) {
                singlePut(s3Client, file, key);
            } else {
                multipartPut(s3Client, file, key);
            }

            String location = "s3://" + bucket + "/" + key;
            log.info("Uploaded {} ({} bytes) to {}", originalName, size, location);
            return "File uploaded successfully: " + location;
        } catch (Exception e) {
            log.error("File upload failed for file: {}", file.getOriginalFilename(), e);
            return "File upload failed: " + e.getMessage();
        }
    }

    private void singlePut(S3Client s3, MultipartFile file, String key) throws IOException {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();
        s3.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
    }

    private void multipartPut(S3Client s3, MultipartFile file, String key) throws IOException {
        String uploadId = s3.createMultipartUpload(CreateMultipartUploadRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build()).uploadId();

        List<CompletedPart> completedParts = new ArrayList<>();
        try (InputStream is = file.getInputStream()) {
            byte[] buffer = new byte[(int) PART_SIZE];
            int partNumber = 1;
            int bytesRead;
            while ((bytesRead = readFully(is, buffer)) > 0) {
                UploadPartResponse partResp = s3.uploadPart(
                        UploadPartRequest.builder()
                                .bucket(bucket)
                                .key(key)
                                .uploadId(uploadId)
                                .partNumber(partNumber)
                                .contentLength((long) bytesRead)
                                .build(),
                        RequestBody.fromInputStream(
                                new java.io.ByteArrayInputStream(buffer, 0, bytesRead), bytesRead));

                completedParts.add(CompletedPart.builder()
                        .partNumber(partNumber)
                        .eTag(partResp.eTag())
                        .build());
                log.debug("Uploaded part {} ({} bytes) of {}", partNumber, bytesRead, key);
                partNumber++;
            }

            s3.completeMultipartUpload(CompleteMultipartUploadRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .uploadId(uploadId)
                    .multipartUpload(CompletedMultipartUpload.builder()
                            .parts(completedParts)
                            .build())
                    .build());
        } catch (Exception e) {
            log.error("Multipart upload failed for {}, aborting upload {}", key, uploadId, e);
            s3.abortMultipartUpload(AbortMultipartUploadRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .uploadId(uploadId)
                    .build());
            throw e;
        }
    }

    /** Reads up to buf.length bytes, possibly across multiple read() calls. */
    private static int readFully(InputStream is, byte[] buf) throws IOException {
        int total = 0;
        while (total < buf.length) {
            int n = is.read(buf, total, buf.length - total);
            if (n < 0) break;
            total += n;
        }
        return total;
    }
}
