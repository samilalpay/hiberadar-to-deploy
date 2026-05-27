package com.hiberadar.common.security;

import org.springframework.stereotype.Component;

@Component
public class SecurityEndpoints {

    private static final String[] PUBLIC = new String[]{
            "/api/auth/**",
            "/actuator/**",
            "/error"
    };

    public String[] publicMatchers() {
        return PUBLIC;
    }

    public boolean isPublic(String path) {
        if (path == null) return false;
        // Basit prefix kontrolü
        return path.startsWith("/api/auth")
                || path.startsWith("/actuator")
                || path.equals("/error");
    }
}
