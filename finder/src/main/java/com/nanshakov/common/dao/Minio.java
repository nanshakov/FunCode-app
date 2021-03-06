package com.nanshakov.common.dao;

import com.nanshakov.common.repo.FileUploader;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.annotation.PostConstruct;

import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidArgumentException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.NoResponseException;
import io.minio.errors.RegionConflictException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@RequiredArgsConstructor
public class Minio implements FileUploader {

    private final MinioClient minioClient;

    @Value("${s3.bucket}")
    private String bucket;

    @PostConstruct
    protected void postConstruct() throws IOException, XmlPullParserException, NoSuchAlgorithmException,
            RegionConflictException, InvalidKeyException, InvalidResponseException, ErrorResponseException,
            NoResponseException, InvalidBucketNameException, InsufficientDataException, InternalException {
        creatBucket(bucket);
    }

    @Override
    public void creatBucket(String name) throws IOException, InvalidKeyException, NoSuchAlgorithmException,
            InsufficientDataException, InvalidResponseException, InternalException, NoResponseException,
            InvalidBucketNameException, XmlPullParserException, ErrorResponseException, RegionConflictException {
        if (minioClient.bucketExists(name)) {
            log.trace("Bucket '{}' already exists.", bucket);
        } else {
            minioClient.makeBucket(name);
        }
    }

    @Override
    public void putObject(String bucketName, String fileName, byte[] bytes) throws IOException, XmlPullParserException,
            NoSuchAlgorithmException, InvalidKeyException, InvalidArgumentException, InvalidResponseException,
            InternalException, NoResponseException, InvalidBucketNameException, InsufficientDataException,
            ErrorResponseException {
        InputStream is = new ByteArrayInputStream(bytes);
        //TODO add contentType by ext
        //image/jpeg
        //
        minioClient.putObject(bucketName, fileName, is, Long.valueOf(is.available()), null, null,
                "application/octet-stream");
        is.close();
    }
}
