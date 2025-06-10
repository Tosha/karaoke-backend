FROM python:3.8-slim

# 1. Install Java 21 (Temurin) and system dependencies
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    wget gnupg2 ca-certificates curl \
    git ffmpeg libsm6 libxext6 && \
    mkdir -p /etc/apt/keyrings && \
    curl -fsSL https://packages.adoptium.net/artifactory/api/gpg/key/public | tee /etc/apt/keyrings/adoptium.asc > /dev/null && \
    echo "deb [signed-by=/etc/apt/keyrings/adoptium.asc] https://packages.adoptium.net/artifactory/deb $(. /etc/os-release && echo $VERSION_CODENAME) main" > /etc/apt/sources.list.d/adoptium.list && \
    apt-get update && \
    apt-get install -y temurin-21-jdk && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# 2. Set up Python environment
RUN python -m venv /venv
ENV PATH="/venv/bin:$PATH"

# 3. Install Python packages
RUN pip install --upgrade pip && \
    pip install spleeter openai-whisper torch torchaudio && \
    pip cache purge

# 4. Verify installations (modified Whisper check)
RUN java -version && \
    spleeter --version && \
    ffmpeg -version && \
    python -c "import whisper; print(f'Whisper version: {whisper.__version__}')"

# 5. Build app
WORKDIR /app
ADD src ./src
ADD .mvn ./.mvn
COPY ["pom.xml", "mvnw", "./"]

EXPOSE 8080
ENTRYPOINT ["./mvnw", "spring-boot:run"]