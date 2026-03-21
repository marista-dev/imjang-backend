FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

# 필요한 디렉토리 생성
RUN mkdir -p /app/uploads /app/logs

# JAR 파일 복사
COPY build/libs/*.jar app.jar

# 볼륨 마운트 포인트
VOLUME ["/app/uploads", "/app/logs"]

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
