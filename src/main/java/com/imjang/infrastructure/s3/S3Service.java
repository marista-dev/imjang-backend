package com.imjang.infrastructure.s3;

import com.imjang.global.config.S3Config;
import com.imjang.global.exception.CustomException;
import com.imjang.global.exception.ErrorCode;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

  private final S3Client s3Client;
  private final S3Config.S3Properties s3Properties;

  /**
   * 이미지 파일을 S3에 업로드
   *
   * @param file
   *         업로드할 파일
   * @param propertyId
   *         매물 ID
   * @param fileName
   *         파일명
   * @return S3 공개 URL
   */
  public String uploadImage(File file, Long propertyId, String fileName) {
    String key = generateImageKey(propertyId, fileName);
    return uploadFile(file, key);
  }

  /**
   * 썸네일 파일을 S3에 업로드
   *
   * @param file
   *         업로드할 썸네일 파일
   * @param propertyId
   *         매물 ID
   * @param fileName
   *         파일명
   * @return S3 공개 URL
   */
  public String uploadThumbnail(File file, Long propertyId, String fileName) {
    String key = generateThumbnailKey(propertyId, fileName);
    return uploadFile(file, key);
  }

  private String uploadFile(File file, String key) {
    try (FileInputStream inputStream = new FileInputStream(file)) {

      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
              .bucket(s3Properties.getS3().getBucket())
              .key(key)
              .build();

      s3Client.putObject(putObjectRequest,
              RequestBody.fromInputStream(inputStream, file.length()));

      String url = generatePublicUrl(key);
      log.info("S3 업로드 성공: {} -> {}", file.getName(), url);
      return url;

    } catch (IOException e) {
      log.error("파일 읽기 실패: {}", file.getName(), e);
      throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
    } catch (S3Exception e) {
      log.error("S3 업로드 실패: {}", key, e);
      throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
    }
  }

  /**
   * 이미지용 S3 키 생성
   */
  private String generateImageKey(Long propertyId, String fileName) {
    return String.format("%s/%d/%s",
            s3Properties.getS3().getImagePrefix(),
            propertyId,
            fileName);
  }

  /**
   * 썸네일용 S3 키 생성
   */
  private String generateThumbnailKey(Long propertyId, String fileName) {
    return String.format("%s/%d/%s",
            s3Properties.getS3().getThumbnailPrefix(),
            propertyId,
            fileName);
  }

  /**
   * S3에서 파일 삭제
   *
   * @param key
   *         S3 객체 키
   */
  public void deleteFile(String key) {
    try {
      DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
              .bucket(s3Properties.getS3().getBucket())
              .key(key)
              .build();

      s3Client.deleteObject(deleteObjectRequest);
      log.info("S3 파일 삭제 성공: {}", key);

    } catch (S3Exception e) {
      log.error("S3 파일 삭제 실패: {}", key, e);
    }
  }

  /**
   * 공개 URL 생성
   */
  private String generatePublicUrl(String key) {
    return String.format("https://%s.s3.%s.amazonaws.com/%s",
            s3Properties.getS3().getBucket(),
            s3Properties.getS3().getRegion(),
            key);
  }
}
