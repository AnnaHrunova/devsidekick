package com.gemini.devsidekick.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ProjectHistoryRequest {
    private LocalDate from;
    private LocalDate to;
}
