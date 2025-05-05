package lv.zemskov.karaoke.repository;

import lv.zemskov.karaoke.model.SeparationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SeparationResultRepository extends JpaRepository<SeparationResult, Long> {
    SeparationResult findByTrackId(UUID id);
}