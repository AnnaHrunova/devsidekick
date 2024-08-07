package com.gemini.devsidekick.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "github")
@Data
public class GithubConfigProperties {

    private String repositoryName;
    private String repositoryOwner;
    private String accessToken;
}
