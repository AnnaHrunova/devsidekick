package com.gemini.devsidekick.service;


import com.gemini.devsidekick.config.ProjectConfigProperties;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.DocsScopes;
import com.google.api.services.docs.v1.model.*;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
public class DocService {
    private static final String APPLICATION_NAME = "Google Docs API";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES =
            Collections.singletonList(DocsScopes.DOCUMENTS_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private final ProjectConfigProperties projectProperties;

    @SneakyThrows
    public String getDocumentContent() {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Docs service = new Docs.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        Document response = service.documents().get(projectProperties.getBrDocId()).execute();
        return readStructuralElements(response.getBody().getContent());
    }

    private static String readStructuralElements(List<StructuralElement> elements) {
        StringBuilder sb = new StringBuilder();
        for (StructuralElement element : elements) {
            if (element.getParagraph() != null) {
                for (ParagraphElement paragraphElement : element.getParagraph().getElements()) {
                    sb.append(readParagraphElement(paragraphElement));
                }
            } else if (element.getTable() != null) {
                for (TableRow row : element.getTable().getTableRows()) {
                    for (TableCell cell : row.getTableCells()) {
                        sb.append(readStructuralElements(cell.getContent()));
                    }
                }
            } else if (element.getTableOfContents() != null) {
                sb.append(readStructuralElements(element.getTableOfContents().getContent()));
            }
        }
        return sb.toString();
    }

    private static String readParagraphElement(ParagraphElement element) {
        TextRun run = element.getTextRun();
        if (run == null || run.getContent() == null) {
            return "";
        }
        return run.getContent();
    }

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        InputStream in = DocService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
}
