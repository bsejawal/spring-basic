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
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;
import java.util.UUID;

@Slf4j
@RestController
public class FileController {

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

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(request,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            String location = "s3://" + bucket + "/" + key;
            log.info("Uploaded {} to {}", originalName, location);
            return "File uploaded successfully: " + location;
        } catch (Exception e) {
            log.error("File upload failed for file: {}", file.getOriginalFilename(), e);
            return "File upload failed: " + e.getMessage();
        }
    }
}