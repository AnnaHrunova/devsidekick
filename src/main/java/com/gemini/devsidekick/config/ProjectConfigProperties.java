package com.gemini.devsidekick.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Configuration
@ConfigurationProperties(prefix = "project")
@Data
public class ProjectConfigProperties {

    private String formattedName;
    private String brDocId;

    @DateTimeFormat(pattern = "MM/dd/yyyy")
    private LocalDate historyFrom;

    @DateTimeFormat(pattern = "MM/dd/yyyy")
    private LocalDate historyTo;
}
