package com.products.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

@Slf4j
@Configuration
@Profile("!test")
public class AwsConfig {
    @Value("${app.aws.s3.bucket.name}")
    private String AWS_BUCKET_NAME;

    private final S3Client s3Client;

    public AwsConfig(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void createDefaultBucket(){
        try {
            ListBucketsResponse response = s3Client.listBuckets();
            if (response.buckets().isEmpty()) {
                createBucket(AWS_BUCKET_NAME);
            }
            else {
                log.info("Default AWS Bucket already exists");
            }
        }
        catch (Exception exception){
            log.error("Error while creating default AWS Bucket", exception);
        }
    }

    @Retryable(maxRetries = 3)
    private void createBucket(String bucketName) {
        try {
            s3Client.createBucket(request -> request.bucket(bucketName));
            log.info("AWS Bucket {} created successfully", bucketName);
        } catch (Exception e){
            log.error("Error while creating AWS Bucket {}", bucketName, e);
        }
    }
}
