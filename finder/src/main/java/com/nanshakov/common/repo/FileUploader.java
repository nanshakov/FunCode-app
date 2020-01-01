package com.nanshakov.common.repo;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.NoResponseException;
import io.minio.errors.RegionConflictException;

public interface FileUploader {

    void creatBucket(String name) throws IOException, InvalidKeyException, NoSuchAlgorithmException,
            InsufficientDataException, InvalidResponseException, InternalException, NoResponseException,
            InvalidBucketNameException, XmlPullParserException, ErrorResponseException, RegionConflictException;

    void putObject(String bucketName, String fileName, byte[] bytes);
}
