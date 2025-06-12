package lv.zemskov.karaoke.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lv.zemskov.karaoke.repository.JobRepository;
import lv.zemskov.karaoke.service.job.DatabaseJobStore;
import lv.zemskov.karaoke.service.job.JobStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class JobStoreConfig {

    @Bean
    public JobStore<UUID> jobStore(JobRepository jobRepository, ObjectMapper objectMapper) {
        return new DatabaseJobStore(jobRepository, objectMapper);
    }
}