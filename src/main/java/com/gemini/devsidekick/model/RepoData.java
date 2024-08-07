package com.gemini.devsidekick.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RepoData {

    private String owner;
    private String name;
    private String accessToken;

    private LocalDate from;
    private LocalDate to;
}
