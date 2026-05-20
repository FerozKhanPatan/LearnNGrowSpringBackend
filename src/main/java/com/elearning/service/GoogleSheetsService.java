package com.elearning.service;

import com.elearning.model.Enrollment;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class GoogleSheetsService {

    private static final String APPLICATION_NAME = "Learn N Byte Enrollment System";
    private static final String CREDENTIALS_PATH = "src/main/resources/credentials.json";
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);

    // Google Sheet ID from the provided URL
    private static final String SPREADSHEET_ID = "1YEvEFOAXatAC0pmdlljrrjp0jYpWaId4QkHCt6ZuVck";
    private static final String SHEET_NAME = "Sheet1";

    /**
     * Get Sheets service using service account credentials
     */
    private Sheets getSheetsService() throws IOException, GeneralSecurityException {
        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(CREDENTIALS_PATH))
                .createScoped(SCOPES);

        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Add enrollment to Google Sheet
     */
    public void addEnrollmentToSheet(Enrollment enrollment, String name, String email) {
        try {
            System.out.println("=== STARTING GOOGLE SHEETS APPEND ===");
            System.out.println("Spreadsheet ID: " + SPREADSHEET_ID);
            System.out.println("Sheet Name: " + SHEET_NAME);

            Sheets sheetsService = getSheetsService();
            System.out.println("Sheets service initialized successfully");

            // Prepare data to be added
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

            System.out.println("Row data size: " + rowData.size());
            System.out.println("Row data: " + rowData);

            // Create value range
            ValueRange body = new ValueRange()
                    .setValues(Collections.singletonList(rowData));

            System.out.println("Value range created, appending to sheet...");

            // Append to sheet
            sheetsService.spreadsheets().values()
                    .append(SPREADSHEET_ID, SHEET_NAME, body)
                    .setValueInputOption("RAW")
                    .execute();

            System.out.println("=== GOOGLE SHEETS APPEND SUCCESS ===");
            System.out.println("Enrollment added to Google Sheet successfully");
            System.out.println("Additional message saved: " + (enrollment.getMessage() != null ? enrollment.getMessage() : "N/A"));
            System.out.println("Submission timestamp: " + enrollmentDate);

        } catch (IOException | GeneralSecurityException e) {
            System.err.println("=== GOOGLE SHEETS APPEND FAILED ===");
            System.err.println("Error type: " + e.getClass().getName());
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to add enrollment to Google Sheet", e);
        }
    }

    /**
     * Get all enrollments from Google Sheet (for syncing status)
     */
    public List<List<Object>> getEnrollmentsFromSheet() {
        try {
            Sheets sheetsService = getSheetsService();

            ValueRange response = sheetsService.spreadsheets().values()
                    .get(SPREADSHEET_ID, SHEET_NAME)
                    .execute();

            List<List<Object>> values = response.getValues();
            if (values == null || values.isEmpty()) {
                System.out.println("No data found in Google Sheet");
                return new ArrayList<>();
            }

            return values;

        } catch (IOException | GeneralSecurityException e) {
            System.err.println("Error reading from Google Sheet: " + e.getMessage());
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
