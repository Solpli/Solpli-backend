package com.ilta.solepli.global.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;

import com.ilta.solepli.global.exception.CustomException;
import com.ilta.solepli.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class S3Service {

  @Value("${cloud.aws.s3.profileBucket}")
  private String profileBucketName;

  @Value("${cloud.aws.s3.reviewBucket}")
  private String reviewBucketName;

  private final AmazonS3 amazonS3;

  public String uploadProfileImage(MultipartFile file) {

    // 파일 확장자 유효성 검사
    validateImageExtension(file.getOriginalFilename());

    // 파일명을 고유하게 지정
    String fileName = createFileName(file.getOriginalFilename());

    // 메타 데이터 추출
    ObjectMetadata objectMetadata = getObjectMetaData(file);

    // S3에 파일 업로드
    try {
      amazonS3.putObject(profileBucketName, fileName, file.getInputStream(), objectMetadata);
    } catch (IOException e) {
      throw new CustomException(ErrorCode.S3_UPLOAD_FAILURE);
    }

    // 업로드된 파일의 URL 반환
    return amazonS3.getUrl(profileBucketName, fileName).toString();
  }

  public String uploadReviewImage(MultipartFile file) {

    // 파일 확장자 유효성 검사
    validateImageExtension(file.getOriginalFilename());

    // 파일명을 고유하게 지정
    String fileName = createFileName(file.getOriginalFilename());

    // 메타 데이터 추출
    ObjectMetadata objectMetadata = getObjectMetaData(file);

    // S3에 파일 업로드
    try {
      amazonS3.putObject(reviewBucketName, fileName, file.getInputStream(), objectMetadata);
    } catch (IOException e) {
      throw new CustomException(ErrorCode.S3_UPLOAD_FAILURE);
    }

    // 업로드된 파일의 URL 반환
    return amazonS3.getUrl(reviewBucketName, fileName).toString();
  }

  public void deleteProfileImage(String fileUrl) {
    try {
      amazonS3.deleteObject(profileBucketName, extractKeyFromUrl(fileUrl));
    } catch (AmazonServiceException e) {
      throw new CustomException(ErrorCode.S3_DELETE_FAILURE);
    }
  }

  public void deleteReviewImage(String fileUrl) {
    try {
      amazonS3.deleteObject(reviewBucketName, extractKeyFromUrl(fileUrl));
    } catch (AmazonServiceException e) {
      throw new CustomException(ErrorCode.S3_DELETE_FAILURE);
    }
  }

  private String extractKeyFromUrl(String url) {
    try {
      URL s3Url = new URL(url);
      return s3Url.getPath().substring(1);
    } catch (MalformedURLException e) {
      throw new CustomException(ErrorCode.MALFORMED_URL_EXCEPTION);
    }
  }

  private void validateImageExtension(String filename) {
    String lowerCaseName = filename.toLowerCase();
    if (!(lowerCaseName.endsWith(".jpg")
        || lowerCaseName.endsWith(".jpeg")
        || lowerCaseName.endsWith(".png"))) {
      throw new CustomException(ErrorCode.UNSUPPORTED_IMAGE_FILE_EXTENSION);
    }
  }

  private String createFileName(String filename) {
    return System.currentTimeMillis() + "_" + filename;
  }

  private ObjectMetadata getObjectMetaData(MultipartFile file) {
    ObjectMetadata objectMetadata = new ObjectMetadata();
    objectMetadata.setContentLength(file.getSize());
    objectMetadata.setContentType(file.getContentType());
    return objectMetadata;
  }
}
