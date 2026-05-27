package com.hiberadar.domain.ingest.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class GrantIngestScheduler {

    private static final Logger log = LoggerFactory.getLogger(GrantIngestScheduler.class);

    private final GrantIngestService grantIngestService;

    public GrantIngestScheduler(GrantIngestService grantIngestService) {
        this.grantIngestService = grantIngestService;
    }

    @Scheduled(cron = "${app.ingest.cron:0 0 */6 * * *}")
    public void runScheduledIngest() {
        try {
            grantIngestService.runNow("SCHEDULER");
        } catch (Exception ex) {
            log.warn("event=INGEST_SCHEDULER_FAILED reason={}", ex.getMessage());
        }
    }
}
