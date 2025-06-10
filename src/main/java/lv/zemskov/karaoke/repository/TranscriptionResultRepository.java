package lv.zemskov.karaoke.repository;

import lv.zemskov.karaoke.model.TranscriptionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TranscriptionResultRepository extends JpaRepository<TranscriptionResult, Long> {
}
