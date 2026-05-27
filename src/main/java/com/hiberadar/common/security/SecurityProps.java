package com.hiberadar.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProps(
        String jwtSecret,
        long jwtTtlMs
) {}
