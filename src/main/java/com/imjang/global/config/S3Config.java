package com.imjang.global.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * AWS S3 설정
 */
@Configuration
@RequiredArgsConstructor
public class S3Config {

  private final S3Properties s3Properties;

  @Bean
  public S3Client s3Client() {
    AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(
            s3Properties.getCredentials().getAccessKey(),
            s3Properties.getCredentials().getSecretKey()
    );

    return S3Client.builder()
            .region(Region.of(s3Properties.getS3().getRegion()))
            .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
            .build();
  }

  @Bean
  public S3Presigner s3Presigner() {
    AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(
            s3Properties.getCredentials().getAccessKey(),
            s3Properties.getCredentials().getSecretKey()
    );

    return S3Presigner.builder()
            .region(Region.of(s3Properties.getS3().getRegion()))
            .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
            .build();
  }

  @ConfigurationProperties(prefix = "cloud.aws")
  @Configuration
  @Getter
  @Setter
  public static class S3Properties {

    private Credentials credentials;
    private S3 s3;

    @Getter
    @Setter
    public static class Credentials {

      private String accessKey;
      private String secretKey;
    }

    @Getter
    @Setter
    public static class S3 {

      private String bucket;
      private String region;
      private String imagePrefix = "images/properties";
      private String thumbnailPrefix = "thumbnails/properties";
    }
  }
}
