package com.hiberadar.domain.ingest.connector;

import com.hiberadar.domain.ingest.connector.enums.ConnectorType;

import java.util.List;

public interface GrantConnector {

    String connectorId();

    ConnectorType connectorType();

    List<ExternalGrantRecord> fetch();
}
