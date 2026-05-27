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
public class CordisApiConnector implements GrantConnector {

    @Override
    public String connectorId() {
        return "CORDIS_API";
    }

    @Override
    public ConnectorType connectorType() {
        return ConnectorType.API;
    }

    @Override
    public List<ExternalGrantRecord> fetch() {
        return List.of(
                new ExternalGrantRecord(
                        "CORDIS",
                        SourceCategory.EU_PROGRAM,
                        SourceScope.INTERNATIONAL,
                        "EU",
                        "https://cordis.europa.eu",
                        "Horizon Europe - Green Manufacturing",
                        "https://cordis.europa.eu/call/he-green-manufacturing",
                        "CORDIS-HE-2026-GM",
                        "Green manufacturing technologies for SME consortia.",
                        "European Commission",
                        "Horizon Europe",
                        "28.99",
                        "EU",
                        InstitutionScope.INTERNATIONAL,
                        "EUR",
                        new BigDecimal("300000"),
                        new BigDecimal("5000000"),
                        LocalDate.now().plusMonths(3),
                        LocalDate.now(),
                        GrantStatus.PUBLISHED
                )
        );
    }
}
