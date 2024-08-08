package com.gemini.devsidekick.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

@Service
@Slf4j
@AllArgsConstructor
public class HistoryService {

    public Pair<String, Map<LocalDate, String>> getSavedSummary(LocalDate from, LocalDate to) {
        try {
            Map<LocalDate, String> res = new LinkedHashMap<>();

            var current = to;
            while (current.isAfter(from.minusDays(1))) {
                var currentDayFileName = historyFileName(current);
                boolean historyExists = new File(currentDayFileName).exists();
                if (historyExists) {
                    var resp = readFromFile(currentDayFileName);
                    res.put(current, resp);
                }
                current = current.minusDays(1);
            }
            return Pair.of(null, res);
        } catch (Exception e) {
            log.error("Error while getting saved repo history:", e);
            return Pair.of(CommonUtilsService.DEFAULT_ERROR_MESSAGE, null);
        }
    }

    public String readFromFile(String fileName) {
        try {
            String data = "";
            File myObj = new File(fileName);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                data = myReader.nextLine();
            }
            myReader.close();
            return data;
        } catch (FileNotFoundException e) {
            log.error("Error while reading from file: ", e);
            return "";
        }
    }

    public void updateHistory(Pair<LocalDate, String> content) {
        try {
            writeToFile(content.getLeft().format(DateTimeFormatter.ISO_LOCAL_DATE) + ".txt", content.getRight());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeToFile(String fileName, String content) throws IOException {
        try {
            File historyFile = new File(fileName);
            if (historyFile.createNewFile()) {
                log.info("File: {} created", historyFile.getName());
            } else {
                log.info("File: {} already exists", historyFile.getName());
            }
            FileWriter myWriter = new FileWriter(fileName);
            myWriter.write(content);
            myWriter.close();
            log.info("Successfully wrote to the file.");
        } catch (IOException e) {
            log.error("An error occurred while writing to file: ", e);
        }
    }

    private static String historyFileName(LocalDate current) {
        return current.format(DateTimeFormatter.ISO_LOCAL_DATE) + ".txt";
    }
}
