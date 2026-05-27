package com.hiberadar.domain.ingest.service;

import com.hiberadar.domain.grant.entity.Grant;
import com.hiberadar.domain.grant.entity.enums.GrantStatus;
import com.hiberadar.domain.grant.entity.enums.InstitutionScope;
import com.hiberadar.domain.grant.repository.GrantRepository;
import com.hiberadar.domain.ingest.connector.ExternalGrantRecord;
import com.hiberadar.domain.ingest.connector.GrantConnector;
import com.hiberadar.domain.ingest.dto.IngestMetricsResponse;
import com.hiberadar.domain.ingest.dto.IngestRunResponse;
import com.hiberadar.domain.ingest.dto.IngestStatusResponse;
import com.hiberadar.domain.ingest.entity.IngestJobRun;
import com.hiberadar.domain.ingest.entity.enums.IngestJobStatus;
import com.hiberadar.domain.ingest.repository.IngestJobRunRepository;
import com.hiberadar.domain.source.entity.Source;
import com.hiberadar.domain.source.repository.SourceRepository;
import com.hiberadar.domain.grant.service.GrantMatchNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class GrantIngestService {

    private static final Logger log = LoggerFactory.getLogger(GrantIngestService.class);

    private final List<GrantConnector> connectors;
    private final GrantRepository grantRepository;
    private final SourceRepository sourceRepository;
    private final IngestJobRunRepository ingestJobRunRepository;
    private final GrantMatchNotificationService grantMatchNotificationService;
    private final ReentrantLock runLock = new ReentrantLock();

    public GrantIngestService(List<GrantConnector> connectors,
                              GrantRepository grantRepository,
                              SourceRepository sourceRepository,
                              IngestJobRunRepository ingestJobRunRepository,
                              GrantMatchNotificationService grantMatchNotificationService) {
        this.connectors = connectors;
        this.grantRepository = grantRepository;
        this.sourceRepository = sourceRepository;
        this.ingestJobRunRepository = ingestJobRunRepository;
        this.grantMatchNotificationService = grantMatchNotificationService;
    }

    @Transactional
    public IngestRunResponse runNow(String triggeredBy) {
        if (!runLock.tryLock()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ingest job is already running");
        }

        IngestJobRun run = new IngestJobRun();
        run.setTriggeredBy(triggeredBy);
        run.setStatus(IngestJobStatus.RUNNING);
        run.setConnectorCount(connectors.size());
        run.setFetchedCount(0);
        run.setCreatedCount(0);
        run.setUpdatedCount(0);
        run.setFailedCount(0);
        run = ingestJobRunRepository.save(run);

        log.info("event=INGEST_RUN_STARTED runId={} trigger={} connectorCount={}",
                run.getId(), triggeredBy, connectors.size());

        List<String> errors = new ArrayList<>();
        try {
            for (GrantConnector connector : connectors) {
                ingestWithConnector(connector, run, errors);
            }
            run.setStatus(errors.isEmpty() ? IngestJobStatus.SUCCESS : IngestJobStatus.FAILED);
        } catch (Exception e) {
            run.setStatus(IngestJobStatus.FAILED);
            run.setFailedCount(run.getFailedCount() + 1);
            errors.add("UNEXPECTED:" + safeMessage(e));
            log.error("event=INGEST_RUN_FAILED runId={} reason={}", run.getId(), safeMessage(e), e);
        } finally {
            run.setFinishedAt(Instant.now());
            run.setErrorMessage(errors.isEmpty() ? null : String.join(" | ", errors));
            run = ingestJobRunRepository.save(run);
            log.info("event=INGEST_RUN_FINISHED runId={} status={} fetched={} created={} updated={} failed={}",
                    run.getId(), run.getStatus(), run.getFetchedCount(), run.getCreatedCount(), run.getUpdatedCount(), run.getFailedCount());
            runLock.unlock();
        }

        return toResponse(run);
    }

    @Transactional(readOnly = true)
    public IngestStatusResponse status() {
        Optional<IngestJobRun> latestOpt = ingestJobRunRepository.findTopByOrderByStartedAtDesc();
        if (latestOpt.isEmpty()) {
            return new IngestStatusResponse(runLock.isLocked(), null, null, null, 0, 0, 0);
        }
        IngestJobRun latest = latestOpt.get();
        return new IngestStatusResponse(
                runLock.isLocked() || latest.getStatus() == IngestJobStatus.RUNNING,
                latest.getStartedAt(),
                latest.getStatus(),
                latest.getErrorMessage(),
                latest.getCreatedCount(),
                latest.getUpdatedCount(),
                latest.getFetchedCount()
        );
    }

    @Transactional(readOnly = true)
    public Page<IngestRunResponse> listRuns(Pageable pageable) {
        return ingestJobRunRepository.findAllByOrderByStartedAtDesc(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public IngestMetricsResponse metrics() {
        List<Object[]> rows = ingestJobRunRepository.aggregateTotals();
        Object[] totals = rows.isEmpty() ? new Object[]{0L, 0L, 0L, 0L} : rows.get(0);
        long totalFetched = toLong(totals[0]);
        long totalCreated = toLong(totals[1]);
        long totalUpdated = toLong(totals[2]);
        long totalFailed = toLong(totals[3]);
        double errorRate = totalFetched == 0 ? 0.0 : (double) totalFailed / (double) totalFetched;
        return new IngestMetricsResponse(totalFetched, totalCreated, totalUpdated, totalFailed, errorRate);
    }

    private void ingestWithConnector(GrantConnector connector, IngestJobRun run, List<String> errors) {
        String connectorId = connector.connectorId();
        log.info("event=INGEST_CONNECTOR_STARTED runId={} connector={} type={}",
                run.getId(), connectorId, connector.connectorType());

        try {
            List<ExternalGrantRecord> records = connector.fetch();
            run.setFetchedCount(run.getFetchedCount() + records.size());
            for (ExternalGrantRecord record : records) {
                upsertRecord(record, run);
            }
            log.info("event=INGEST_CONNECTOR_FINISHED runId={} connector={} fetched={}",
                    run.getId(), connectorId, records.size());
        } catch (Exception e) {
            run.setFailedCount(run.getFailedCount() + 1);
            String err = connectorId + ":" + safeMessage(e);
            errors.add(err);
            log.error("event=INGEST_CONNECTOR_FAILED runId={} connector={} reason={}",
                    run.getId(), connectorId, safeMessage(e), e);
        }
    }

    private void upsertRecord(ExternalGrantRecord record, IngestJobRun run) {
        Source source = resolveSource(record);

        Optional<Grant> existing = Optional.empty();
        if (hasText(record.referenceCode())) {
            existing = grantRepository.findBySourceIdAndReferenceCodeIgnoreCase(source.getId(), record.referenceCode());
        }
        if (existing.isEmpty() && hasText(record.officialUrl())) {
            existing = grantRepository.findBySourceIdAndOfficialUrlIgnoreCase(source.getId(), record.officialUrl());
        }
        if (existing.isEmpty()) {
            existing = grantRepository.findFirstBySourceIdAndTitleIgnoreCase(source.getId(), record.title());
        }

        if (existing.isPresent()) {
            Grant g = existing.get();
            applyGrantFields(g, source, record);
            grantRepository.save(g);
            run.setUpdatedCount(run.getUpdatedCount() + 1);
            return;
        }

        Grant g = new Grant();
        applyGrantFields(g, source, record);
        Grant saved = grantRepository.save(g);
        run.setCreatedCount(run.getCreatedCount() + 1);
        if (saved.getStatus() == GrantStatus.PUBLISHED) {
            grantMatchNotificationService.notifyMatchingFirms(saved);
        }
    }

    private Source resolveSource(ExternalGrantRecord record) {
        Optional<Source> byName = hasText(record.sourceName())
                ? sourceRepository.findByNameIgnoreCase(record.sourceName())
                : Optional.empty();
        Optional<Source> byUrl = hasText(record.sourceOfficialUrl())
                ? sourceRepository.findByOfficialUrlIgnoreCase(record.sourceOfficialUrl())
                : Optional.empty();

        return byName
                .or(() -> byUrl)
                .orElseGet(() -> {
                    Source s = new Source();
                    s.setName(record.sourceName());
                    s.setCategory(record.sourceCategory());
                    s.setScope(record.sourceScope());
                    s.setCountryCode(normalize(record.sourceCountryCode()));
                    s.setOfficialUrl(record.sourceOfficialUrl());
                    s.setActive(true);
                    return sourceRepository.save(s);
                });
    }

    private void applyGrantFields(Grant g, Source source, ExternalGrantRecord record) {
        g.setSource(source);
        g.setTitle(record.title());
        g.setOfficialUrl(record.officialUrl());
        g.setReferenceCode(record.referenceCode());
        g.setSummaryShort(record.summary());
        g.setProviderName(record.providerName());
        g.setProgramName(record.programName());
        g.setNaceCode(normalize(record.naceCode()));
        g.setCountryCode(normalize(record.countryCode()));
        g.setCurrency(normalize(record.currency()));
        g.setFundingMin(record.fundingMin());
        g.setFundingMax(record.fundingMax());
        g.setDeadlineAt(record.deadlineAt());

        InstitutionScope scope = record.scope();
        if (scope == null) {
            String sourceScope = source.getScope() == null ? "INTERNATIONAL" : source.getScope().name();
            scope = "NATIONAL".equals(sourceScope)
                    ? InstitutionScope.NATIONAL
                    : InstitutionScope.INTERNATIONAL;
        }
        g.setScope(scope);

        GrantStatus resolvedStatus = resolveStatus(record.status(), record.deadlineAt());
        g.setStatus(resolvedStatus);

        if (record.publishedAt() != null) {
            g.setPublishedAt(record.publishedAt());
        } else if (resolvedStatus == GrantStatus.PUBLISHED && g.getPublishedAt() == null) {
            g.setPublishedAt(LocalDate.now());
        }
    }

    private GrantStatus resolveStatus(GrantStatus statusFromSource, LocalDate deadlineAt) {
        if (statusFromSource != null) {
            return statusFromSource;
        }
        if (deadlineAt != null && deadlineAt.isBefore(LocalDate.now())) {
            return GrantStatus.CLOSED;
        }
        return GrantStatus.PUBLISHED;
    }

    private String normalize(String value) {
        if (!hasText(value)) {
            return value;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number n) {
            return n.longValue();
        }
        return Long.parseLong(value.toString());
    }

    private String safeMessage(Exception e) {
        String msg = e.getMessage();
        return (msg == null || msg.isBlank()) ? e.getClass().getSimpleName() : msg;
    }

    private IngestRunResponse toResponse(IngestJobRun run) {
        return new IngestRunResponse(
                run.getId(),
                run.getTriggeredBy(),
                run.getStatus(),
                run.getStartedAt(),
                run.getFinishedAt(),
                run.getConnectorCount(),
                run.getFetchedCount(),
                run.getCreatedCount(),
                run.getUpdatedCount(),
                run.getFailedCount(),
                run.getErrorMessage()
        );
    }
}
