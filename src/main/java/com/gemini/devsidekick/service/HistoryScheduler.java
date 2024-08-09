package com.gemini.devsidekick.service;

import com.gemini.devsidekick.config.GithubConfigProperties;
import com.gemini.devsidekick.config.ProjectConfigProperties;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@AllArgsConstructor
public class HistoryScheduler {

    private final GeminiService geminiService;
    private final HistoryService historyService;
    private final ProjectConfigProperties projectProperties;

    @Scheduled(fixedRate = 24 * 60 * 60 * 1000, fixedDelay = 60000)
    public void scheduleHistoryUpdate() {
        var from = LocalDate.now();
        var to = LocalDate.now();

        if (projectProperties.getHistoryFrom() != null) {
            from = projectProperties.getHistoryFrom();
        }

        if (projectProperties.getHistoryTo() != null) {
            to = projectProperties.getHistoryTo();
        }

        var current = from;
        while (current.isBefore(to.plusDays(1))) {
            boolean historyExists = new File("src/main/resources/doc/history/" + current.format(DateTimeFormatter.ISO_LOCAL_DATE) + ".txt").exists();
            if (!historyExists) {
                from = current;
                to = current;
                var content = geminiService.getLiveSummary(from, to);
                if (content.getRight() != null && !content.getRight().isEmpty()) {
                    var item = content.getRight().entrySet().iterator().next();
                    historyService.updateHistory(Pair.of(item.getKey(), item.getValue()));
                }
            }
            current = current.plusDays(1);
        }
    }
}
