# -------- Stage 1: Python + Spleeter --------
FROM python:3.10-slim as spleeter-builder

# Install ffmpeg and spleeter dependencies
RUN apt-get update && \
    apt-get install -y ffmpeg git build-essential && \
    pip install spleeter && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# -------- Stage 2: Java App Base --------
FROM eclipse-temurin:24-jdk

# Set working directory
WORKDIR /app

# Copy Spleeter + ffmpeg from stage 1
COPY --from=spleeter-builder /usr/local /usr/local
COPY --from=spleeter-builder /usr/bin/ffmpeg /usr/bin/ffmpeg
COPY --from=spleeter-builder /usr/bin/ffprobe /usr/bin/ffprobe

# Copy the Spring Boot app JAR file
COPY target/karaoke-service-0.0.1-SNAPSHOT.jar app.jar

# Expose the app port
EXPOSE 8080

# Start the app
ENTRYPOINT ["java", "-jar", "app.jar"]