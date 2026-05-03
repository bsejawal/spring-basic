package com.bsejawal.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.CompletionException;

/**
 * Builds the {@link S3AsyncClient} bean and the underlying Netty HTTP client.
 * Multipart upload is enabled and tuned via {@link S3Properties} so large
 * files are split into parallel parts by the SDK.
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(S3Properties.class)
@RequiredArgsConstructor
public class S3Config {

    private static final long MB = 1024L * 1024L;

    private final S3Properties props;

    @Bean(destroyMethod = "close")
    public SdkAsyncHttpClient s3HttpClient() {
        return NettyNioAsyncHttpClient.builder()
                .maxConcurrency(props.getHttpMaxConcurrency())
                .maxPendingConnectionAcquires(props.getHttpMaxPendingAcquires())
                .connectionAcquisitionTimeout(
                        Duration.ofSeconds(props.getHttpAcquireTimeoutSeconds()))
                .readTimeout(Duration.ofSeconds(props.getHttpReadTimeoutSeconds()))
                .writeTimeout(Duration.ofSeconds(props.getHttpWriteTimeoutSeconds()))
                .build();
    }

    @Bean(destroyMethod = "close")
    public S3AsyncClient s3AsyncClient(SdkAsyncHttpClient httpClient) {
        S3AsyncClient client = S3AsyncClient.builder()
                .httpClient(httpClient)
                .endpointOverride(URI.create(props.getEndpoint()))
                .region(Region.of(props.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey())))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(props.isPathStyleAccess())
                        .build())
                .multipartEnabled(true)
                .multipartConfiguration(b -> b
                        .thresholdInBytes(props.getMultipartThresholdMb() * MB)
                        .minimumPartSizeInBytes(props.getMultipartPartSizeMb() * MB))
                .build();

        if (props.isAutoCreateBucket()) {
            ensureBucket(client);
        }
        return client;
    }

    private void ensureBucket(S3AsyncClient client) {
        String bucket = props.getBucket();
        try {
            client.headBucket(HeadBucketRequest.builder().bucket(bucket).build()).join();
            log.info("S3 bucket '{}' is reachable", bucket);
        } catch (CompletionException ce) {
            Throwable cause = ce.getCause() != null ? ce.getCause() : ce;
            if (cause instanceof NoSuchBucketException) {
                log.info("Bucket '{}' not found, creating it", bucket);
                client.createBucket(CreateBucketRequest.builder().bucket(bucket).build()).join();
            } else {
                throw new IllegalStateException(
                        "Failed to verify S3 bucket '" + bucket + "'", cause);
            }
        }
    }
}
