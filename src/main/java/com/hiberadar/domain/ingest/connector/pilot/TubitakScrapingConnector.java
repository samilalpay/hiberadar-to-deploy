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
public class TubitakScrapingConnector implements GrantConnector {

    @Override
    public String connectorId() {
        return "TUBITAK_SCRAPE";
    }

    @Override
    public ConnectorType connectorType() {
        return ConnectorType.SCRAPING;
    }

    @Override
    public List<ExternalGrantRecord> fetch() {
        return List.of(
                new ExternalGrantRecord(
                        "TUBITAK",
                        SourceCategory.PUBLIC_INSTITUTION,
                        SourceScope.NATIONAL,
                        "TR",
                        "https://www.tubitak.gov.tr",
                        "1507 KOBI Ar-Ge Baslangic Destek Programi",
                        "https://www.tubitak.gov.tr/1507",
                        "TUBITAK-1507-2026-1",
                        "KOBI olcekli firmalar icin Ar-Ge proje destek cagrisi.",
                        "TUBITAK",
                        "1507",
                        "72.19",
                        "TR",
                        InstitutionScope.NATIONAL,
                        "TRY",
                        new BigDecimal("50000"),
                        new BigDecimal("1200000"),
                        LocalDate.now().plusMonths(1),
                        LocalDate.now(),
                        GrantStatus.PUBLISHED
                )
        );
    }
}
