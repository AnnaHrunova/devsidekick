package com.gemini.devsidekick.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "gemini")
@Data
public class GeminiConfigProperties {

    private boolean liveMode;
    private String apiKey;
}
