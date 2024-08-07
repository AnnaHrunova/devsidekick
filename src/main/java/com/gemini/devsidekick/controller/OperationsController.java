package com.gemini.devsidekick.controller;

import com.gemini.devsidekick.config.GeminiConfigProperties;
import com.gemini.devsidekick.config.ProjectConfigProperties;
import com.gemini.devsidekick.model.BrDocumentData;
import com.gemini.devsidekick.model.ProjectHistoryRequest;
import com.gemini.devsidekick.service.GeminiService;
import com.gemini.devsidekick.service.GithubService;
import com.gemini.devsidekick.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDate;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class OperationsController {

    private final GeminiService geminiService;
    private final GithubService githubService;
    private final HistoryService historyService;
    private final ProjectConfigProperties projectProperties;
    private final GeminiConfigProperties geminiProperties;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("projectName", "Project: " + projectProperties.getFormattedName());

        var historyRequest = new ProjectHistoryRequest();
        model.addAttribute("historyRequest", historyRequest);

        var docData = new BrDocumentData();
        docData.setBrDocUrl("https://docs.google.com/document/d/" + projectProperties.getBrDocId());
        model.addAttribute("brDocData", docData);
        return "home";
    }

    @PostMapping("/summary")
    public String getSavedSummary(@ModelAttribute ProjectHistoryRequest request, Model model) {
        Pair<String, Map<LocalDate, String>> diff;
        if (geminiProperties.isLiveMode()) {
            diff = geminiService.getLiveSummary(request.getFrom(), request.getTo());
        } else {
            diff = historyService.getSavedSummary(request.getFrom(), request.getTo());
        }

        model.addAttribute("message", diff.getLeft());
        model.addAttribute("resDiff", diff.getRight());

        return "result";
    }

    @PostMapping("/compare")
    public String compare(@ModelAttribute BrDocumentData brDocUrl, Model model) {
        var initDate = githubService.getCommitInitDate();
        var diff = geminiService.compareRepoAndBr(initDate, LocalDate.now());
        model.addAttribute("message", diff);

        return "compare";
    }

}