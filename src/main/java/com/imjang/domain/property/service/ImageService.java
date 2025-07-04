package com.imjang.domain.property.service;

import com.imjang.domain.auth.entity.User;
import com.imjang.domain.auth.repository.UserRepository;
import com.imjang.domain.property.dto.response.ImageUploadResponse;
import com.imjang.domain.property.entity.TempImage;
import com.imjang.domain.property.repository.TempImageRepository;
import com.imjang.global.exception.CustomException;
import com.imjang.global.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {

  private final TempImageRepository tempImageRepository;
  private final UserRepository userRepository;

  @Value("${app.upload.path:uploads}")
  private String uploadPath;

  @Value("${app.upload.max-size:10485760}") // 10MB
  private long maxFileSize;

  @Value("${app.thumbnail.width:300}")
  private int thumbnailWidth;

  @Value("${app.thumbnail.height:300}")
  private int thumbnailHeight;

  @Value("${app.thumbnail.format:png}")
  private String thumbnailFormat;

  private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp");

  /**
   * 서비스 초기화 시 업로드 디렉토리 생성
   */
  @PostConstruct
  public void init() {
    try {
      Path uploadDir = Paths.get(uploadPath).toAbsolutePath();
      if (!Files.exists(uploadDir)) {
        Files.createDirectories(uploadDir);
        log.info("업로드 디렉토리 생성: {}", uploadDir);
      } else {
        log.info("Upload directory exists: {}", uploadDir);
      }
    } catch (IOException e) {
      log.error("Failed to create upload directory", e);
    }
  }

  /**
   * 이미지 업로드 및 썸네일 생성
   *
   * TODO: Phase 2 - 썸네일 생성을 비동기 처리로 전환
   * TODO: Phase 3 - CDN 기반 이미지 리사이징 적용
   */
  @Transactional
  public ImageUploadResponse uploadImage(MultipartFile file, Long userId) {
    validateFile(file);

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    try {
      // 파일명 생성
      String fileName = generateFileName(file.getOriginalFilename());
      Path originalPath = createFilePath(fileName);
      String thumbnailFileName = createThumbnailFileName(fileName);
      Path thumbnailPath = createFilePath(thumbnailFileName);

      // 원본 파일 저장
      file.transferTo(originalPath.toFile());
      log.debug("📂파일 저장 경로: {}", originalPath);

      // 썸네일 생성
      createThumbnail(originalPath.toFile(), thumbnailPath.toFile());
      log.debug("🖼️썸네일 저장 경로: {}", thumbnailPath);

      // DB에 임시 이미지 정보 저장
      TempImage tempImage = TempImage.builder()
              .user(user)
              .originalUrl(originalPath.toString())
              .thumbnailUrl(thumbnailPath.toString())
              .expiresAt(LocalDateTime.now().plusHours(24))
              .build();

      tempImage = tempImageRepository.save(tempImage);
      log.info("임시이미지 저장 완료: {}", tempImage.getId());

      // 웹 접근 가능한 URL 생성
      LocalDate now = LocalDate.now();
      String webAccessibleUrl = String.format("/images/%d/%02d/%02d/%s",
              now.getYear(), now.getMonthValue(), now.getDayOfMonth(), thumbnailFileName);

      return ImageUploadResponse.of(tempImage.getId(), webAccessibleUrl);

    } catch (IOException e) {
      log.error("Failed to upload image", e);
      throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
    }
  }

  /**
   * 파일 검증
   */
  private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new CustomException(ErrorCode.EMPTY_FILE);
    }

    if (file.getSize() > maxFileSize) {
      throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED);
    }

    String filename = file.getOriginalFilename();
    if (filename == null || !hasAllowedExtension(filename)) {
      throw new CustomException(ErrorCode.INVALID_FILE_TYPE);
    }
  }

  /**
   * 파일 확장자 검증
   */
  private boolean hasAllowedExtension(String filename) {
    String extension = getFileExtension(filename).toLowerCase();
    return ALLOWED_EXTENSIONS.contains(extension);
  }

  /**
   * 파일 확장자 추출
   */
  private String getFileExtension(String filename) {
    int lastDotIndex = filename.lastIndexOf(".");
    return (lastDotIndex == -1) ? "" : filename.substring(lastDotIndex + 1);
  }

  /**
   * 고유한 파일명 생성
   */
  private String generateFileName(String originalFilename) {
    String extension = getFileExtension(originalFilename);
    return UUID.randomUUID().toString() + "." + extension;
  }

  /**
   * 안전한 썸네일 파일명 생성
   */
  private String createThumbnailFileName(String originalFileName) {
    int lastDotIndex = originalFileName.lastIndexOf(".");
    if (lastDotIndex == -1) {
      return "thumb_" + originalFileName + "." + thumbnailFormat;
    }
    String baseName = originalFileName.substring(0, lastDotIndex);
    return "thumb_" + baseName + "." + thumbnailFormat;
  }

  /**
   * 파일 저장 경로 생성
   */
  private Path createFilePath(String filename) throws IOException {
    // 절대 경로 사용
    Path basePath = Paths.get(uploadPath).toAbsolutePath();

    // 날짜별 디렉토리 생성 (예: uploads/2024/01/15/)
    LocalDate now = LocalDate.now();
    Path directoryPath = basePath.resolve(
                    String.valueOf(now.getYear()))
            .resolve(String.format("%02d", now.getMonthValue()))
            .resolve(String.format("%02d", now.getDayOfMonth()));

    // 디렉토리가 없으면 생성
    if (!Files.exists(directoryPath)) {
      Files.createDirectories(directoryPath);
      log.debug("Created directory: {}", directoryPath);
    }

    return directoryPath.resolve(filename);
  }

  /**
   * 썸네일 생성 (Thumbnailator 라이브러리 사용)
   * 외부 설정에 따라 썸네일 크기와 포맷을 동적으로 설정
   *
   * @param originalFile
   *         원본 파일
   * @param thumbnailFile
   *         썸네일 저장 경로
   */
  private void createThumbnail(File originalFile, File thumbnailFile) throws IOException {
    Thumbnails.of(originalFile)
            .size(thumbnailWidth, thumbnailHeight)
            .keepAspectRatio(true)
            .outputFormat(thumbnailFormat)
            .toFile(thumbnailFile);
  }
}
