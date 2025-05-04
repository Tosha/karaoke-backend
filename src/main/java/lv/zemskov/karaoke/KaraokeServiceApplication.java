package lv.zemskov.karaoke;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KaraokeServiceApplication {

	private static final Logger logger = LoggerFactory.getLogger(KaraokeServiceApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(KaraokeServiceApplication.class, args);
	}

	@PostConstruct
	public void validateEnvironment() {
		try {
			// 1. Verify Python is accessible
			Process pythonCheck = new ProcessBuilder("python", "--version").start();
			if (pythonCheck.waitFor() != 0) {
				throw new IllegalStateException("Python not found");
			}

			// 2. Verify spleeter installation
			Process cliCheck = new ProcessBuilder("spleeter", "--version").start();
			if (cliCheck.waitFor() != 0) {
				throw new IllegalStateException("Spleeter CLI not configured");
			}

			// 3. Verify FFmpeg
			Process ffmpegCheck = new ProcessBuilder("ffmpeg", "-version").start();
			if (ffmpegCheck.waitFor() != 0) {
				throw new IllegalStateException("FFmpeg not found");
			}

			logger.info("All dependencies verified");
		} catch (Exception e) {
			logger.error("Dependency validation failed", e);
			throw new IllegalStateException("System check failed: " + e.getMessage());
		}
	}

}
