package com.gemini.devsidekick.service;

import com.gemini.devsidekick.config.GithubConfigProperties;
import com.gemini.devsidekick.model.RepoData;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.springframework.http.HttpHeaders.USER_AGENT;

@Service
@AllArgsConstructor
@Slf4j
public class GithubService {

    private final GithubConfigProperties githubConfigProperties;

    public Map<LocalDate, List<String>> getDiff(LocalDate from, LocalDate to) {
        var fromFormatted = from.format(DateTimeFormatter.ISO_DATE) + "T00:00:00Z";
        var toFormatted = to.format(DateTimeFormatter.ISO_DATE) + "T23:59:59Z";

        var url = String.format("https://api.github.com/repos/%s/%s/commits?since=%s&until=%s",
                githubConfigProperties.getRepositoryOwner(), githubConfigProperties.getRepositoryName(), fromFormatted, toFormatted);
        var response = askGithub(githubConfigProperties.getAccessToken(), url);
        return getCommitUrlsPerDate(response, from, to);
    }

    public LocalDate getCommitInitDate() {
        var url = String.format("https://api.github.com/repos/%s/%s",
                githubConfigProperties.getRepositoryOwner(), githubConfigProperties.getRepositoryName());
        var response = askGithub(githubConfigProperties.getAccessToken(), url);
        if (response == null) {
            return null;
        }
        return getInitDate(response);
    }

    private static String askGithub(String accessToken, String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
            connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString(("username:" + accessToken).getBytes()));

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response.toString();

            } else {
                log.error("Error while calling Github API: {}", responseCode);
                return null;
            }
        } catch (Exception e) {
            log.error("Error while calling Github API: ", e);
            return null;
        }
    }

    private static Map<LocalDate, List<String>> getCommitUrlsPerDate(String jsonResponse, LocalDate from, LocalDate to) {
        try {
            Map<LocalDate, List<String>> result = new HashMap<>();
            LocalDate d = from;
            while (d.isBefore(to.plusDays(1))) {
                result.put(d, new ArrayList<>());
                d = d.plusDays(1);
            }
            org.json.JSONArray commits = new org.json.JSONArray(jsonResponse);
            for (int i = 0; i < commits.length(); i++) {
                org.json.JSONObject commit = commits.getJSONObject(i);
                var url = commit.getString("html_url");
                var commitDate = extractCommitDate(commit);
                result.get(commitDate).add(url);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed request...");
        }
    }

    private static LocalDate getInitDate(String jsonResponse) {
        try {
            org.json.JSONObject createdDate = new org.json.JSONObject(jsonResponse);
            var dateTime = LocalDateTime.parse(createdDate.getString("created_at"), DateTimeFormatter.ISO_ZONED_DATE_TIME);
            return LocalDate.of(dateTime.getYear(), dateTime.getMonth(), dateTime.getDayOfMonth());
        } catch (Exception e) {
            throw new RuntimeException("Failed request...");
        }
    }

    private static LocalDate extractCommitDate(JSONObject commit) {
        var commitDate = commit.getJSONObject("commit")
                .getJSONObject("author").getString("date");
        return LocalDate.parse(commitDate,
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
    }
}
