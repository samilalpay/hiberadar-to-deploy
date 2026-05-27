package com.hiberadar.domain.ingest.repository;

import com.hiberadar.domain.ingest.entity.IngestJobRun;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface IngestJobRunRepository extends JpaRepository<IngestJobRun, Long> {

    Optional<IngestJobRun> findTopByOrderByStartedAtDesc();

    Page<IngestJobRun> findAllByOrderByStartedAtDesc(Pageable pageable);

    @Query("""
            select coalesce(sum(j.fetchedCount), 0),
                   coalesce(sum(j.createdCount), 0),
                   coalesce(sum(j.updatedCount), 0),
                   coalesce(sum(j.failedCount), 0)
            from IngestJobRun j
            """)
    java.util.List<Object[]> aggregateTotals();
}
