package com.hiberadar.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.upload")
public class UploadProperties {

    private String baseDir = "uploads";
    private String institutionsDir = "institutions";
    private String firmLogosDir = "firm-logos";

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public String getInstitutionsDir() {
        return institutionsDir;
    }

    public void setInstitutionsDir(String institutionsDir) {
        this.institutionsDir = institutionsDir;
    }

    public String getFirmLogosDir() {
        return firmLogosDir;
    }

    public void setFirmLogosDir(String firmLogosDir) {
        this.firmLogosDir = firmLogosDir;
    }
}
