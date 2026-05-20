package com.elearning.service;

import com.elearning.model.Enrollment;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class GoogleSheetsService {

    private static final String APPLICATION_NAME = "Learn N Byte Enrollment System";
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);

    // Google Sheet ID from the provided URL
    private static final String SPREADSHEET_ID = "1YEvEFOAXatAC0pmdlljrrjp0jYpWaId4QkHCt6ZuVck";
    private static final String SHEET_NAME = "Sheet1";

    // LAZY INITIALIZATION: No constructor, no static init, no @PostConstruct
    // All Google setup happens ONLY inside addEnrollmentToSheet()

    /**
     * Add enrollment to Google Sheet
     * LAZY INITIALIZATION: All Google Sheets setup happens here at runtime
     */
    public void addEnrollmentToSheet(Enrollment enrollment, String name, String email) {
        System.out.println("=== LAZY INITIALIZATION: Starting Google Sheets append ===");
        
        try {
            // Load credentials here (LAZY)
            System.out.println("STEP 1: Loading credentials...");
            InputStream credentialsStream = null;
            String credentialsPath = "";
            
            try {
                // Try classpath resource first (for production JAR)
                System.out.println("Attempting to load credentials from classpath...");
                ClassPathResource resource = new ClassPathResource("credentials.json");
                System.out.println("ClassPathResource exists: " + resource.exists());
                
                if (resource.exists()) {
                    credentialsStream = resource.getInputStream();
                    credentialsPath = "classpath:credentials.json";
                    System.out.println("Credentials loaded from classpath: " + credentialsPath);
                } else {
                    System.out.println("Credentials not found in classpath, trying file system...");
                    // Try file system (for local development)
                    File file = new File("src/main/resources/credentials.json");
                    System.out.println("Checking file: " + file.getAbsolutePath());
                    System.out.println("File exists: " + file.exists());
                    if (file.exists()) {
                        credentialsStream = new FileInputStream(file);
                        credentialsPath = file.getAbsolutePath();
                        System.out.println("Credentials loaded from file: " + credentialsPath);
                    } else {
                        // Try absolute path
                        file = new File("credentials.json");
                        System.out.println("Checking file: " + file.getAbsolutePath());
                        System.out.println("File exists: " + file.exists());
                        if (file.exists()) {
                            credentialsStream = new FileInputStream(file);
                            credentialsPath = file.getAbsolutePath();
                            System.out.println("Credentials loaded from current directory: " + credentialsPath);
                        } else {
                            System.err.println("ERROR: Credentials file not found in any location");
                            System.err.println("Checked locations:");
                            System.err.println("  - classpath:credentials.json");
                            System.err.println("  - src/main/resources/credentials.json");
                            System.err.println("  - credentials.json");
                            throw new IOException("Credentials file not found in any location");
                        }
                    }
                }
                
                System.out.println("Credentials stream loaded successfully from: " + credentialsPath);
                System.out.println("Stream available: " + (credentialsStream != null && credentialsStream.available() > 0));
                
                GoogleCredential credential = GoogleCredential.fromStream(credentialsStream)
                        .createScoped(SCOPES);
                
                System.out.println("Credential created with scopes: " + SCOPES);
                System.out.println("Service account email: " + credential.getServiceAccountId());
                System.out.println("STEP 1 COMPLETED: Credentials loaded successfully");
                
                // Create Sheets service here (LAZY)
                System.out.println("STEP 2: Creating Sheets service...");
                Sheets sheetsService = new Sheets.Builder(
                        GoogleNetHttpTransport.newTrustedTransport(),
                        GsonFactory.getDefaultInstance(),
                        credential)
                        .setApplicationName(APPLICATION_NAME)
                        .build();
                
                System.out.println("STEP 2 COMPLETED: Sheets service initialized successfully");
                
                // Prepare data
                System.out.println("STEP 3: Preparing enrollment data...");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String enrollmentDate = enrollment.getEnrollmentDate().format(formatter);

                List<Object> rowData = Arrays.asList(
                    enrollment.getUser() != null ? enrollment.getUser().getId().toString() : "GUEST",
                    name,
                    email,
                    enrollment.getPhoneNumber() != null ? enrollment.getPhoneNumber() : "",
                    enrollment.getCourseId() != null ? enrollment.getCourseId() : "",
                    enrollment.getCourseTitle() != null ? enrollment.getCourseTitle() : "",
                    enrollment.getStatus().toString(),
                    enrollmentDate,
                    enrollment.getMessage() != null ? enrollment.getMessage() : "",
                    enrollmentDate
                );

                System.out.println("STEP 3 COMPLETED: Row data prepared");

                // Create value range
                System.out.println("STEP 4: Creating value range...");
                ValueRange body = new ValueRange()
                        .setValues(Collections.singletonList(rowData));

                System.out.println("STEP 4 COMPLETED: Value range created");

                // Execute append here (LAZY)
                System.out.println("STEP 5: Executing Google Sheets API append...");
                var response = sheetsService.spreadsheets().values()
                        .append(SPREADSHEET_ID, SHEET_NAME, body)
                        .setValueInputOption("RAW")
                        .execute();
                
                System.out.println("STEP 5 COMPLETED: API execute() returned");
                System.out.println("API Response: " + response);
                System.out.println("Updates: " + response.getUpdates());
                if (response.getUpdates() != null) {
                    System.out.println("Updated rows: " + response.getUpdates().getUpdatedRows());
                    System.out.println("Updated columns: " + response.getUpdates().getUpdatedColumns());
                    System.out.println("Updated cells: " + response.getUpdates().getUpdatedCells());
                }

                System.out.println("=== LAZY INITIALIZATION: Google Sheets append SUCCESS ===");
                
            } finally {
                if (credentialsStream != null) {
                    try {
                        credentialsStream.close();
                    } catch (IOException e) {
                        System.err.println("Error closing credentials stream: " + e.getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("=== LAZY INITIALIZATION: Google Sheets append FAILED ===");
            System.err.println("Error type: " + e.getClass().getName());
            System.err.println("Error message: " + e.getMessage());
            System.err.println("Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "N/A"));
            System.err.println("Full stack trace:");
            e.printStackTrace();
            // Do NOT re-throw - enrollment should still succeed
        }
    }

    // Other methods temporarily removed to avoid getSheetsService() dependency
    // These can be re-added later with lazy initialization if needed
}
