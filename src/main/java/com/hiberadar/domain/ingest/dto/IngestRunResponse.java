package com.hiberadar.domain.ingest.dto;

import com.hiberadar.domain.ingest.entity.enums.IngestJobStatus;

import java.time.Instant;

public record IngestRunResponse(
        Long id,
        String triggeredBy,
        IngestJobStatus status,
        Instant startedAt,
        Instant finishedAt,
        int connectorCount,
        int fetchedCount,
        int createdCount,
        int updatedCount,
        int failedCount,
        String errorMessage
) {
}
