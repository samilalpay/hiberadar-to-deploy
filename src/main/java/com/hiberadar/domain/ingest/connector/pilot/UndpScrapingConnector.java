package com.hiberadar.domain.ingest.connector.pilot;

import com.hiberadar.domain.grant.entity.enums.GrantStatus;
import com.hiberadar.domain.grant.entity.enums.InstitutionScope;
import com.hiberadar.domain.ingest.connector.ExternalGrantRecord;
import com.hiberadar.domain.ingest.connector.GrantConnector;
import com.hiberadar.domain.ingest.connector.enums.ConnectorType;
import com.hiberadar.domain.source.entity.enums.SourceCategory;
import com.hiberadar.domain.source.entity.enums.SourceScope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class UndpScrapingConnector implements GrantConnector {

    @Override
    public String connectorId() {
        return "UNDP_SCRAPE";
    }

    @Override
    public ConnectorType connectorType() {
        return ConnectorType.SCRAPING;
    }

    @Override
    public List<ExternalGrantRecord> fetch() {
        return List.of(
                new ExternalGrantRecord(
                        "UNDP",
                        SourceCategory.UN_PROGRAM,
                        SourceScope.INTERNATIONAL,
                        "UN",
                        "https://www.undp.org",
                        "Digital Inclusion Innovation Challenge",
                        "https://www.undp.org/digital-inclusion-challenge",
                        "UNDP-DIGI-2026-01",
                        "Innovation grants for inclusive digital products.",
                        "UNDP",
                        "Innovation Challenge",
                        "62.02",
                        "UN",
                        InstitutionScope.INTERNATIONAL,
                        "USD",
                        new BigDecimal("25000"),
                        new BigDecimal("300000"),
                        LocalDate.now().plusMonths(4),
                        LocalDate.now(),
                        GrantStatus.PUBLISHED
                )
        );
    }
}
