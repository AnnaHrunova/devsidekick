package com.gemini.devsidekick.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

@Component
@Slf4j
public class CommonUtilsService {

    public static final String DEFAULT_ERROR_MESSAGE = "Unexpected error occurred. Please, try later!";

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
            log.error("An error occurred while reading file: {}", fileName, e);
            return "";
        }
    }

    public String removeExtraSpacing(String str) {
        return str.replace("\n", "")
                .replace("\\n", "")
                .replace("\"", "")
                .replace("\\", "")
                .replace("*", "");
    }
}
