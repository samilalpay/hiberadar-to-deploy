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
public class KosgebApiConnector implements GrantConnector {

    @Override
    public String connectorId() {
        return "KOSGEB_API";
    }

    @Override
    public ConnectorType connectorType() {
        return ConnectorType.API;
    }

    @Override
    public List<ExternalGrantRecord> fetch() {
        return List.of(
                new ExternalGrantRecord(
                        "KOSGEB",
                        SourceCategory.GOV_PORTAL,
                        SourceScope.NATIONAL,
                        "TR",
                        "https://www.kosgeb.gov.tr",
                        "KOBI Dijital Donusum Destegi",
                        "https://www.kosgeb.gov.tr/dijital-donusum",
                        "KOSGEB-2026-DD-01",
                        "KOBI'lerin dijital donusum projelerine finansman destegi.",
                        "KOSGEB",
                        "Dijital Donusum Programi",
                        "62.01",
                        "TR",
                        InstitutionScope.NATIONAL,
                        "TRY",
                        new BigDecimal("100000"),
                        new BigDecimal("2000000"),
                        LocalDate.now().plusMonths(2),
                        LocalDate.now(),
                        GrantStatus.PUBLISHED
                )
        );
    }
}
