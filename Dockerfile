FROM python:3.8-slim

# 1. Install Java 21 (Temurin)
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    wget gnupg2 ca-certificates curl && \
    mkdir -p /etc/apt/keyrings && \
    curl -fsSL https://packages.adoptium.net/artifactory/api/gpg/key/public | tee /etc/apt/keyrings/adoptium.asc > /dev/null && \
    echo "deb [signed-by=/etc/apt/keyrings/adoptium.asc] https://packages.adoptium.net/artifactory/deb $(. /etc/os-release && echo $VERSION_CODENAME) main" > /etc/apt/sources.list.d/adoptium.list && \
    apt-get update && \
    apt-get install -y temurin-21-jdk ffmpeg && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# 2. Install Spleeter via pip
RUN python -m venv /venv && \
    . /venv/bin/activate && \
    pip install --upgrade pip && \
    pip install spleeter

ENV PATH="/venv/bin:$PATH"

# 3. Verify installs
RUN java -version && \
    spleeter --version && \
    ffmpeg -version

# 4. Build app
WORKDIR /app
ADD src ./src
ADD .mvn ./.mvn
COPY ["pom.xml", "mvnw", "./"]

EXPOSE 8080
ENTRYPOINT ["./mvnw", "spring-boot:run"]
