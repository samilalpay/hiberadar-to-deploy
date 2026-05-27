package com.hiberadar.domain.ingest.dto;

public record IngestMetricsResponse(
        long totalFetchedCount,
        long totalCreatedCount,
        long totalUpdatedCount,
        long totalFailedCount,
        double errorRate
) {
}
