package com.products.application.service;

import com.products.application.exception.BadGatewayException;
import com.products.application.exception.ProductImageNotFoundException;
import io.awspring.cloud.s3.S3Template;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
public class S3BucketService implements BucketService{
    @Value("${app.aws.s3.bucket.name}")
    private String BUCKET_NAME;

    private final S3Template s3Template;
    private final S3Client s3Client;

    public S3BucketService(S3Template s3Template, S3Client s3Client) {
        this.s3Template = s3Template;
        this.s3Client = s3Client;
    }

    @Override
    public URL getReadPreSignedURL(String key) {
        return s3Template.createSignedGetURL(
                BUCKET_NAME, key, Duration.ofMinutes(15)
        );
    }

    @Override
    public List<URL> getReadPreSignedURLByPrefix(String prefix) {
        return List.of();
    }

    @Override
    public URL getUploadPreSignedURL(String key) {
        return s3Template.createSignedPutURL(
                BUCKET_NAME, key, Duration.ofMinutes(15)
        );
    }

    @Override
    public void deleteObject(String key) {
        s3Template.deleteObject(BUCKET_NAME, key);
    }

    @Override
    public void deleteAllObjectsByPrefix(String keyPrefix) {
        ListObjectsV2Response listObjectsResponse = s3Client.listObjectsV2(
                ListObjectsV2Request.builder()
                        .bucket(BUCKET_NAME)
                        .prefix(keyPrefix)
                        .build()
        );

        if (listObjectsResponse.contents().isEmpty())
            throw new ProductImageNotFoundException("Image not found");

        List<ObjectIdentifier> identifiers = listObjectsResponse.contents().stream()
                .map(object -> ObjectIdentifier.builder()
                        .key(object.key())
                        .build()
                )
                .toList();

        DeleteObjectsRequest deleteObjectsRequest = DeleteObjectsRequest.builder()
                .bucket(BUCKET_NAME)
                .delete(
                        Delete.builder()
                                .objects(identifiers)
                                .build()
                )
                .build();

        try {
            s3Client.deleteObjects(deleteObjectsRequest);
        } catch (Exception e){
            throw new BadGatewayException("Failed to delete objects from S3 bucket");
        }
    }
}
