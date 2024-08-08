package com.gemini.devsidekick.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "google-docs")
@Data
public class GoogleDocConfigProperties {

    private boolean liveMode;
    private String brDocUrl;
}
