package com.bsejawal.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for the S3 storage backend.
 * Bound from {@code aws.s3.*} in application.yml.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "aws.s3")
public class S3Properties {

    /** Service endpoint, e.g. http://localhost:4566 for LocalStack. */
    @NotBlank
    private String endpoint;

    /** AWS region (used for signing). */
    @NotBlank
    private String region;

    /** Access key id. Use env vars / secrets manager in production. */
    @NotBlank
    private String accessKey;

    /** Secret access key. Use env vars / secrets manager in production. */
    @NotBlank
    private String secretKey;

    /** Target bucket name. */
    @NotBlank
    private String bucket;

    /** Optional key prefix (logical "folder"). May be blank. */
    private String folder = "";

    /** Path-style access is required for LocalStack. */
    private boolean pathStyleAccess = true;

    /** Whether to create the bucket on startup if it does not exist. */
    private boolean autoCreateBucket = true;

    /** Max concurrent connections in the Netty HTTP client pool. */
    private int httpMaxConcurrency = 200;

    /** Max queued connection-acquire requests before failing fast. */
    private int httpMaxPendingAcquires = 10_000;

    /** How long to wait for a free connection before timing out (seconds). */
    private int httpAcquireTimeoutSeconds = 60;

    /** Read and write timeouts for individual HTTP operations (seconds). */
    private int httpReadTimeoutSeconds = 300;
    private int httpWriteTimeoutSeconds = 300;

    /** Files smaller than this use a single PutObject; larger files use multipart upload. */
    private long multipartThresholdMb = 16;

    /** Each multipart part is this many MB (S3 minimum is 5 MB; cap is 5 GB). */
    private long multipartPartSizeMb = 16;
}

