package com.warrantyvault.service;

import com.warrantyvault.config.StorageProperties;
import com.warrantyvault.exception.StorageException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class StorageService {

    private final S3Client s3;
    private final StorageProperties properties;

    @PostConstruct
    void createBucketIfMissing() {
        if (!properties.createBucket()) {
            return;
        }
        try {
            s3.headBucket(b -> b.bucket(properties.bucket()));
        } catch (NoSuchBucketException ex) {
            s3.createBucket(b -> b.bucket(properties.bucket()));
            log.info("Created bucket {}", properties.bucket());
        }
    }

    public void put(String key, MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            s3.putObject(b -> b.bucket(properties.bucket()).key(key)
                            .contentType(file.getContentType())
                            .contentLength(file.getSize()),
                    RequestBody.fromInputStream(in, file.getSize()));
        } catch (IOException | S3Exception ex) {
            throw new StorageException("Could not store " + key, ex);
        }
    }

    public InputStream open(String key) {
        try {
            return s3.getObject(b -> b.bucket(properties.bucket()).key(key));
        } catch (S3Exception ex) {
            throw new StorageException("Could not read " + key, ex);
        }
    }

    public void delete(String key) {
        try {
            s3.deleteObject(b -> b.bucket(properties.bucket()).key(key));
        } catch (S3Exception ex) {
            log.warn("Could not delete object {}", key, ex);
        }
    }

    public void deleteAfterCommit(List<String> keys) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                keys.forEach(StorageService.this::delete);
            }
        });
    }

    public void deleteOnRollback(String key) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    delete(key);
                }
            }
        });
    }
}
