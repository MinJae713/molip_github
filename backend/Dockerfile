# 1. 베이스 이미지 설정 (Java 21, 리눅스 환경)
FROM eclipse-temurin:21-jdk-jammy

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. 빌드된 JAR 파일을 컨테이너 내부로 복사
# Maven 빌드 시 생성되는 target 폴더 내의 JAR 경로를 기본값으로 설정합니다.
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /app.jar

# 4. 타임존 설정 (한국 시간)
ENV TZ=Asia/Seoul

# 5. 실행 명령어
# -Duser.timezone: JVM 내부 시간 설정
# -Dspring.profiles.active=prod: 운영 환경 프로파일 적용
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "-Dspring.profiles.active=prod", "/app.jar"]
