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

    /**
     * Get Sheets service using service account credentials
     */
    private Sheets getSheetsService() throws IOException, GeneralSecurityException {
        System.out.println("=== INITIALIZING GOOGLE SHEETS SERVICE ===");
        System.out.println("Current working directory: " + System.getProperty("user.dir"));
        
        // Try multiple credential paths
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
            System.out.println("Service account ID: " + credential.getServiceAccountId());

            Sheets sheetsService = new Sheets.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            
            System.out.println("Sheets service initialized successfully");
            return sheetsService;
            
        } catch (IOException e) {
            System.err.println("ERROR loading credentials: " + e.getMessage());
            System.err.println("Error type: " + e.getClass().getName());
            e.printStackTrace();
            throw e;
        } finally {
            if (credentialsStream != null) {
                try {
                    credentialsStream.close();
                } catch (IOException e) {
                    System.err.println("Error closing credentials stream: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Add enrollment to Google Sheet
     * STAGE 1: Only logging - no Sheets service, no credentials, no append
     */
    public void addEnrollmentToSheet(Enrollment enrollment, String name, String email) {
        System.out.println("=== STAGE 1: METHOD addEnrollmentToSheet ENTERED ===");
        System.out.println("Enrollment object: " + enrollment);
        System.out.println("Name: " + name);
        System.out.println("Email: " + email);
        System.out.println("=== STAGE 1: METHOD addEnrollmentToSheet EXITING (NO OP) ===");
        // STAGE 1: Do nothing - just log entry
    }

    /**
     * Test connection to Google Sheets
     */
    public boolean testConnection() {
        try {
            System.out.println("=== TESTING GOOGLE SHEETS CONNECTION ===");
            Sheets sheetsService = getSheetsService();
            
            // Try to read the spreadsheet metadata
            var spreadsheet = sheetsService.spreadsheets().get(SPREADSHEET_ID).execute();
            System.out.println("Spreadsheet title: " + spreadsheet.getProperties().getTitle());
            System.out.println("Spreadsheet URL: " + spreadsheet.getSpreadsheetUrl());
            
            // List all sheets
            System.out.println("Available sheets:");
            for (var sheet : spreadsheet.getSheets()) {
                System.out.println("  - " + sheet.getProperties().getTitle());
            }
            
            // Check if target sheet exists
            boolean sheetExists = spreadsheet.getSheets().stream()
                .anyMatch(sheet -> sheet.getProperties().getTitle().equals(SHEET_NAME));
            
            if (sheetExists) {
                System.out.println("Target sheet '" + SHEET_NAME + "' exists");
            } else {
                System.err.println("WARNING: Target sheet '" + SHEET_NAME + "' does not exist!");
                System.err.println("Available sheets: " + 
                    spreadsheet.getSheets().stream()
                        .map(sheet -> sheet.getProperties().getTitle())
                        .toList());
            }
            
            System.out.println("=== GOOGLE SHEETS CONNECTION TEST SUCCESS ===");
            return sheetExists;
            
        } catch (IOException | GeneralSecurityException e) {
            System.err.println("=== GOOGLE SHEETS CONNECTION TEST FAILED ===");
            System.err.println("Error type: " + e.getClass().getName());
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all enrollments from Google Sheet (for syncing status)
     */
    public List<List<Object>> getEnrollmentsFromSheet() {
        try {
            System.out.println("=== READING FROM GOOGLE SHEET ===");
            Sheets sheetsService = getSheetsService();

            ValueRange response = sheetsService.spreadsheets().values()
                    .get(SPREADSHEET_ID, SHEET_NAME)
                    .execute();
            
            System.out.println("API Response: " + response);

            List<List<Object>> values = response.getValues();
            if (values == null || values.isEmpty()) {
                System.out.println("No data found in Google Sheet");
                return new ArrayList<>();
            }
            
            System.out.println("Found " + values.size() + " rows in Google Sheet");
            return values;

        } catch (IOException | GeneralSecurityException e) {
            System.err.println("Error reading from Google Sheet: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to read from Google Sheet", e);
        }
    }

    /**
     * Update enrollment status in Google Sheet
     */
    public void updateEnrollmentStatusInSheet(int rowIndex, String newStatus) {
        try {
            Sheets sheetsService = getSheetsService();

            // Status is typically in column H (index 7, 0-based)
            String range = SHEET_NAME + "!H" + (rowIndex + 1);

            List<Object> rowData = Collections.singletonList(newStatus);
            ValueRange body = new ValueRange()
                    .setValues(Collections.singletonList(rowData));

            sheetsService.spreadsheets().values()
                    .update(SPREADSHEET_ID, range, body)
                    .setValueInputOption("RAW")
                    .execute();

            System.out.println("Enrollment status updated in Google Sheet successfully");

        } catch (IOException | GeneralSecurityException e) {
            System.err.println("Error updating enrollment status in Google Sheet: " + e.getMessage());
            throw new RuntimeException("Failed to update enrollment status in Google Sheet", e);
        }
    }
}
