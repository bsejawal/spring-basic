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
}
