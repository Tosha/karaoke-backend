package lv.zemskov.karaoke.repository;

import lv.zemskov.karaoke.model.LyricsSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LyricsSegmentRepository extends JpaRepository<LyricsSegment, Long> {
}
