# -------- Stage 1: Spleeter Base --------
FROM researchdeezer/spleeter:latest as spleeter-base

# -------- Stage 2: Java + Spleeter --------
FROM eclipse-temurin:24-jdk

# Install system dependencies (FFmpeg + Python)
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    ffmpeg \
    python3 \
    python3-pip && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Copy Python environment from Spleeter base
COPY --from=spleeter-base /usr/local/lib/python3.7/dist-packages/ /usr/local/lib/python3.7/dist-packages/
COPY --from=spleeter-base /usr/local/bin/spleeter /usr/local/bin/

# Symlink Python3 to Python (required by Spleeter)
RUN ln -s /usr/bin/python3 /usr/local/bin/python

# Create working directories
RUN mkdir -p /app/input /app/output
WORKDIR /app

# Copy Spring Boot application
COPY target/karaoke-service-0.0.1-SNAPSHOT.jar app.jar

# Set environment variables
ENV INPUT_DIR=/app/input
ENV OUTPUT_DIR=/app/output

# Expose port and run
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]