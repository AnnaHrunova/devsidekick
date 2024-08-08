package com.gemini.devsidekick.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gemini.devsidekick.config.GeminiConfigProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor
public class GeminiService {

    private final GeminiConfigProperties properties;
    private final GithubService githubService;
    private final GoogleDocsService docService;
    private final CommonUtilsService utilsService;

    public Pair<String, Map<LocalDate, String>> getLiveSummary(LocalDate from, LocalDate to) {

        if (from.isAfter(to) || from.isAfter(LocalDate.now())) {
            return Pair.of("Incorrect time range", null);
        }
        var commits = githubService.getDiff(from, to);
        if (commits == null) {
            return Pair.of(CommonUtilsService.DEFAULT_ERROR_MESSAGE, null);
        }
        if (commits.isEmpty()) {
            return Pair.of("No commits for selected time range.", null);
        }
        try {
            Map<LocalDate, String> res = new HashMap<>();
            commits.entrySet()
                    .stream()
                    .filter(c -> !c.getValue().isEmpty())
                    .forEach(e -> {
                        var response = askGeminiApi("Please, briefly describe changes made in those commits in as a list without title " + e.getValue());
                        res.put(e.getKey(), response);
                    });
            return Pair.of(null, res);
        } catch (Exception e) {
            log.error("Error while handling Gemini API response:", e);
            return Pair.of(CommonUtilsService.DEFAULT_ERROR_MESSAGE, null);
        }

    }

    private String askGeminiApi(String question) {
        var apiUrl = String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s", properties.getModel(), properties.getApiKey());
        var jsonRequestBody = "{\"contents\":[{\"parts\":[{\"text\":\"" + question + "\"}]}]}";

        HttpEntity<String> request =
                new HttpEntity<>(jsonRequestBody);
        var response = new RestTemplate()
                .postForEntity(URI.create(apiUrl), request, String.class);
        String responseBody = response.getBody();

        try {
            return String.valueOf(new ObjectMapper().readTree(responseBody)
                    .get("candidates").get(0)
                    .get("content")
                    .get("parts").get(0)
                    .get("text"));
        } catch (JsonProcessingException exception) {
            throw new RuntimeException(exception);
        }
    }

    public String compareRepoAndBr(String brDocUrl, LocalDate from, LocalDate to) {

        StringBuilder repoSummary = new StringBuilder();
        var current = to;
        while (current.isAfter(from.minusDays(1))) {
            boolean historyExists = new File(current.format(DateTimeFormatter.ISO_LOCAL_DATE) + ".txt").exists();
            if (historyExists) {
                var resp = utilsService.readFromFile(current.format(DateTimeFormatter.ISO_LOCAL_DATE) + ".txt");
                repoSummary.append(resp);
            }
            current = current.minusDays(1);
        }
        var repoSummaryFormat = utilsService.removeExtraSpacing(repoSummary.toString());
        var brDoc = utilsService.removeExtraSpacing(docService.getDocumentContent(brDocUrl));

        var question = "There are project business requirements: " + brDoc + ". Which features described in business requirements are missing in this project description: " + repoSummaryFormat;
        return askGeminiApi(question);
    }
}
