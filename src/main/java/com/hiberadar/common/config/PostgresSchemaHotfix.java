package com.hiberadar.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class PostgresSchemaHotfix {

    private static final Logger log = LoggerFactory.getLogger(PostgresSchemaHotfix.class);

    private final JdbcTemplate jdbcTemplate;
    private final String datasourceUrl;

    public PostgresSchemaHotfix(JdbcTemplate jdbcTemplate,
            @Value("${spring.datasource.url:}") String datasourceUrl) {
        this.jdbcTemplate = jdbcTemplate;
        this.datasourceUrl = datasourceUrl;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void fixUsernameColumnTypeIfNeeded() {
        if (datasourceUrl == null || !datasourceUrl.startsWith("jdbc:postgresql:")) {
            return;
        }

        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                """
                        select table_schema, data_type
                        from information_schema.columns
                        where table_name = 'app_users'
                          and column_name = 'username'
                        """);

        if (columns.isEmpty()) {
            log.warn("Schema hotfix skipped: app_users.username column not found.");
            return;
        }

        for (Map<String, Object> row : columns) {
            String schema = String.valueOf(row.get("table_schema"));
            String dataType = String.valueOf(row.get("data_type"));

            if (!"bytea".equalsIgnoreCase(dataType)) {
                log.info("Schema hotfix check: {}.app_users.username type is {}, no change needed.", schema, dataType);
                continue;
            }

            // Guard against unsafe identifier injection before building ALTER statement.
            if (!schema.matches("[A-Za-z0-9_]+")) {
                log.error("Schema hotfix skipped for unsafe schema name: {}", schema);
                continue;
            }

            log.warn("Detected {}.app_users.username as bytea. Applying conversion to varchar(120).", schema);
            String alterSqlUtf8 = """
                    alter table %s.app_users
                    alter column username type varchar(120)
                    using convert_from(username, 'UTF8')
                    """.formatted(schema);
            try {
                jdbcTemplate.execute(Objects.requireNonNull(alterSqlUtf8));
                log.warn("Schema hotfix applied with UTF8 conversion on {}.app_users.username.", schema);
            } catch (Exception ex) {
                log.warn("UTF8 conversion failed for {}.app_users.username, trying byte-escape fallback: {}", schema,
                        ex.getMessage());
                String alterSqlEscape = """
                        alter table %s.app_users
                        alter column username type varchar(120)
                        using encode(username, 'escape')
                        """.formatted(schema);
                jdbcTemplate.execute(Objects.requireNonNull(alterSqlEscape));
                log.warn("Schema hotfix applied with escape conversion on {}.app_users.username.", schema);
            }
        }
    }
}
