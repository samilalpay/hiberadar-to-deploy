package com.hiberadar.domain.ingest.dto;

import com.hiberadar.domain.ingest.entity.enums.IngestJobStatus;

import java.time.Instant;

public record IngestStatusResponse(
        boolean running,
        Instant lastRunAt,
        IngestJobStatus lastStatus,
        String lastError,
        int lastCreatedCount,
        int lastUpdatedCount,
        int lastFetchedCount
) {
}
