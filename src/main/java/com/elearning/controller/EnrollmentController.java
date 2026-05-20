package com.elearning.controller;

import com.elearning.dto.EnrollmentRequestDTO;
import com.elearning.dto.EnrollmentResponseDTO;
import com.elearning.model.Course;
import com.elearning.model.Enrollment;
import com.elearning.model.User;
import com.elearning.repository.CourseRepository;
import com.elearning.repository.EnrollmentRepository;
import com.elearning.repository.UserRepository;
import com.elearning.service.GoogleSheetsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/enrollments")
@CrossOrigin(origins = "*")
public class EnrollmentController {

    private static final Logger logger = LoggerFactory.getLogger(EnrollmentController.class);

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private GoogleSheetsService googleSheetsService;

    // Create enrollment (for both logged-in and guest users)
    @PostMapping
    public ResponseEntity<?> createEnrollment(@RequestBody EnrollmentRequestDTO request) {
        System.out.println("=== ENROLLMENT REQUEST START ===");
        System.out.println("REQUEST BODY = " + request);
        logger.info("=== ENROLLMENT REQUEST START ===");
        logger.info("Received enrollment request: {}", request);
        logger.info("Request details - courseId={}, userId={}, email={}, name={}, phoneNumber={}",
            request.getCourseId(), request.getUserId(), request.getEmail(), request.getName(), request.getPhoneNumber());

        try {
            // Validate required fields
            if (request.getCourseId() == null || request.getCourseId().isEmpty()) {
                logger.warn("Enrollment request missing courseId");
                System.out.println("ERROR: Course ID is missing or empty");
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Course ID is required"));
            }

            if (request.getEmail() == null || request.getEmail().isEmpty()) {
                logger.warn("Enrollment request missing email");
                System.out.println("ERROR: Email is missing or empty");
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Email is required"));
            }

            // Fetch course from database using courseCode
            logger.info("Looking up course with code: {}", request.getCourseId());
            System.out.println("Looking up course with code: " + request.getCourseId());
            Optional<Course> courseOptional = courseRepository.findByCourseCode(request.getCourseId());
            if (courseOptional.isEmpty()) {
                logger.warn("Course not found with code: {}", request.getCourseId());
                System.out.println("ERROR: Course not found with code: " + request.getCourseId());
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Course not found with code: " + request.getCourseId()));
            }
            Course course = courseOptional.get();
            logger.info("Found course: id={}, code={}, name={}", course.getId(), course.getCourseCode(), course.getName());
            System.out.println("Found course: id=" + course.getId() + ", code=" + course.getCourseCode() + ", name=" + course.getName());

            // Check if user is logged in
            User user = null;
            if (request.getUserId() != null) {
                logger.info("Looking up user by ID: {}", request.getUserId());
                System.out.println("Looking up user by ID: " + request.getUserId());
                Optional<User> userOptional = userRepository.findById(request.getUserId());
                if (userOptional.isPresent()) {
                    user = userOptional.get();
                    logger.info("Found logged-in user: id={}, email={}", user.getId(), user.getEmail());
                    System.out.println("Found logged-in user: id=" + user.getId() + ", email=" + user.getEmail());
                } else {
                    logger.warn("User ID {} not found in database", request.getUserId());
                    System.out.println("WARNING: User ID " + request.getUserId() + " not found in database");
                }
            }

            // For guest users, check if email already exists (to prevent duplicate guest enrollments)
            if (user == null) {
                logger.info("Looking up user by email: {}", request.getEmail());
                System.out.println("Looking up user by email: " + request.getEmail());
                Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
                if (existingUser.isPresent()) {
                    user = existingUser.get();
                    logger.info("Found existing user by email: id={}, email={}", user.getId(), user.getEmail());
                    System.out.println("Found existing user by email: id=" + user.getId() + ", email=" + user.getEmail());
                } else {
                    // Create new user automatically if email doesn't exist
                    logger.info("No existing user found with email: {}. Creating new user.", request.getEmail());
                    System.out.println("Creating new user with email: " + request.getEmail());
                    User newUser = new User();
                    newUser.setName(request.getName());
                    newUser.setEmail(request.getEmail());
                    newUser.setPassword("temp123"); // Temporary password (not encoded for now)
                    newUser.setRole(User.UserRole.STUDENT);
                    user = userRepository.save(newUser);
                    logger.info("Created new user: id={}, email={}, role={}", user.getId(), user.getEmail(), user.getRole());
                    System.out.println("Created new user: id=" + user.getId() + ", email=" + user.getEmail());
                }
            }

            logger.info("Resolved user: id={}, email={}", user.getId(), user.getEmail());
            System.out.println("Resolved user: id=" + user.getId() + ", email=" + user.getEmail());

            if (user == null) {
                logger.error("User resolved as NULL before enrollment save");
                System.out.println("ERROR: User resolved as NULL before enrollment save");
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to resolve user for enrollment"));
            }

            // Check for duplicate enrollment
            if (user != null) {
                logger.info("Checking for duplicate enrollment for user id={}", user.getId());
                System.out.println("Checking for duplicate enrollment for user id=" + user.getId());
                List<Enrollment> existingEnrollments = enrollmentRepository.findByUserId(user.getId());
                logger.info("Found {} existing enrollments for user", existingEnrollments.size());
                System.out.println("Found " + existingEnrollments.size() + " existing enrollments for user");
                for (Enrollment existing : existingEnrollments) {
                    logger.info("Checking existing enrollment: courseId={}, status={}", existing.getCourseId(), existing.getStatus());
                    System.out.println("Checking existing enrollment: courseId=" + existing.getCourseId() + ", status=" + existing.getStatus());
                    if (existing.getCourseId() != null && existing.getCourseId().equals(request.getCourseId())) {
                        logger.warn("Duplicate enrollment detected for user {} and course {}", user.getId(), request.getCourseId());
                        System.out.println("ERROR: Duplicate enrollment detected for user " + user.getId() + " and course " + request.getCourseId());
                        return ResponseEntity.badRequest()
                            .body(Map.of("success", false, "message", "You are already enrolled in this course"));
                    }
                }
            }

            // Create enrollment
            logger.info("Creating enrollment entity...");
            System.out.println("Creating enrollment entity...");
            Enrollment enrollment = new Enrollment();
            enrollment.setUser(user);
            enrollment.setCourse(course);
            enrollment.setCourseId(request.getCourseId());
            enrollment.setCourseTitle(request.getCourseTitle() != null ? request.getCourseTitle() : course.getName());
            enrollment.setEnrollmentDate(LocalDateTime.now());
            enrollment.setStatus(Enrollment.EnrollmentStatus.PENDING);
            enrollment.setProgress(0);
            enrollment.setPhoneNumber(request.getPhoneNumber());
            enrollment.setMessage(request.getMessage());

            logger.info("Enrollment entity before save: user={}, course={}, courseId={}, courseTitle={}, status={}, phoneNumber={}, message={}",
                enrollment.getUser() != null ? enrollment.getUser().getId() : null,
                enrollment.getCourse() != null ? enrollment.getCourse().getId() : null,
                enrollment.getCourseId(),
                enrollment.getCourseTitle(),
                enrollment.getStatus(),
                enrollment.getPhoneNumber(),
                enrollment.getMessage());
            System.out.println("Enrollment entity before save: user=" + (enrollment.getUser() != null ? enrollment.getUser().getId() : null) +
                ", course=" + (enrollment.getCourse() != null ? enrollment.getCourse().getId() : null) +
                ", courseId=" + enrollment.getCourseId() +
                ", courseTitle=" + enrollment.getCourseTitle() +
                ", status=" + enrollment.getStatus());

            logger.info("Enrollment user ID before save: {}", enrollment.getUser().getId());
            System.out.println("Enrollment user ID before save: " + enrollment.getUser().getId());

            logger.info("Saving enrollment to database...");
            System.out.println("Saving enrollment to database...");
            Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
            logger.info("Enrollment saved successfully with ID: {}", savedEnrollment.getId());
            System.out.println("Enrollment saved successfully with ID: " + savedEnrollment.getId());

            // Submit to Google Sheets (non-blocking) - STAGE 1: Only method call with logging
            try {
                logger.info("=== STAGE 1: GOOGLE SHEETS SUBMISSION START ===");
                System.out.println("=== STAGE 1: GOOGLE SHEETS SUBMISSION START ===");
                logger.info("Submitting enrollment to Google Sheets (STAGE 1 - logging only)...");
                System.out.println("Submitting enrollment to Google Sheets (STAGE 1 - logging only)...");
                logger.info("Enrollment ID: {}", savedEnrollment.getId());
                System.out.println("Enrollment ID: " + savedEnrollment.getId());
                logger.info("Name: {}", request.getName());
                System.out.println("Name: " + request.getName());
                logger.info("Email: {}", request.getEmail());
                System.out.println("Email: " + request.getEmail());
                
                googleSheetsService.addEnrollmentToSheet(savedEnrollment, request.getName(), request.getEmail());
                
                logger.info("=== STAGE 1: GOOGLE SHEETS SUBMISSION COMPLETED ===");
                System.out.println("=== STAGE 1: GOOGLE SHEETS SUBMISSION COMPLETED ===");
                logger.info("Google Sheets submission successful (STAGE 1)");
                System.out.println("Google Sheets submission successful (STAGE 1)");
            } catch (Exception e) {
                // Google Sheets failure MUST NOT fail the entire enrollment
                logger.error("=== STAGE 1: GOOGLE SHEETS SUBMISSION FAILED (ENROLLMENT STILL SUCCESSFUL) ===");
                logger.error("Failed to add to Google Sheets: {}", e.getMessage(), e);
                System.err.println("=== STAGE 1: GOOGLE SHEETS SUBMISSION FAILED (ENROLLMENT STILL SUCCESSFUL) ===");
                System.err.println("Failed to add to Google Sheets: " + e.getMessage());
                System.err.println("Exception type: " + e.getClass().getName());
                System.err.println("Exception message: " + e.getMessage());
                System.err.println("Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "N/A"));
                System.err.println("Cause type: " + (e.getCause() != null ? e.getCause().getClass().getName() : "N/A"));
                System.err.println("Full stack trace:");
                e.printStackTrace();
                // IMPORTANT: Do NOT re-throw - enrollment should still succeed
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Submitted successfully. You will get a call soon.");
            response.put("enrollment", EnrollmentResponseDTO.fromEntity(savedEnrollment));

            logger.info("=== ENROLLMENT REQUEST SUCCESSFUL ===");
            System.out.println("=== ENROLLMENT REQUEST SUCCESSFUL ===");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("=== ENROLLMENT REQUEST FAILED ===");
            logger.error("Exception type: {}", e.getClass().getName());
            logger.error("Exception message: {}", e.getMessage());
            logger.error("Full stack trace:", e);
            System.err.println("=== ENROLLMENT REQUEST FAILED ===");
            System.err.println("Exception type: " + e.getClass().getName());
            System.err.println("Exception message: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "message", "Failed to submit enrollment. Please try again.",
                    "error", e.getMessage(),
                    "type", e.getClass().getName()
                ));
        }
    }

    // Get enrollments by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserEnrollments(@PathVariable Long userId) {
        logger.info("Fetching enrollments for userId={}", userId);
        try {
            List<Enrollment> enrollments = enrollmentRepository.findByUserId(userId);
            List<EnrollmentResponseDTO> dtoList = enrollments.stream()
                .map(EnrollmentResponseDTO::fromEntity)
                .toList();
            logger.info("Found {} enrollments for userId={}", dtoList.size(), userId);
            return ResponseEntity.ok(dtoList);
        } catch (Exception e) {
            logger.error("Failed to fetch enrollments for userId={}: {}", userId, e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Failed to fetch enrollments: " + e.getMessage()));
        }
    }

    // Check if user is enrolled in a specific course
    @GetMapping("/check")
    public ResponseEntity<?> checkEnrollment(
            @RequestParam Long userId,
            @RequestParam String courseId) {
        logger.info("Checking enrollment for userId={}, courseId={}", userId, courseId);
        try {
            List<Enrollment> enrollments = enrollmentRepository.findByUserId(userId);
            for (Enrollment enrollment : enrollments) {
                if (enrollment.getCourseId() != null && enrollment.getCourseId().equals(courseId)) {
                    logger.info("User {} is enrolled in course {} with status {}", userId, courseId, enrollment.getStatus());
                    return ResponseEntity.ok(Map.of(
                        "enrolled", true,
                        "status", enrollment.getStatus().toString(),
                        "enrollmentId", enrollment.getId()
                    ));
                }
            }
            logger.info("User {} is not enrolled in course {}", userId, courseId);
            return ResponseEntity.ok(Map.of("enrolled", false));
        } catch (Exception e) {
            logger.error("Failed to check enrollment for userId={}, courseId={}: {}", userId, courseId, e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Failed to check enrollment: " + e.getMessage()));
        }
    }

    // Update enrollment status (for admin use)
    @PutMapping("/{enrollmentId}/status")
    public ResponseEntity<?> updateEnrollmentStatus(
            @PathVariable Long enrollmentId,
            @RequestBody Map<String, String> statusData) {
        logger.info("Updating enrollment status for enrollmentId={}, status={}", enrollmentId, statusData.get("status"));
        try {
            Optional<Enrollment> enrollmentOptional = enrollmentRepository.findById(enrollmentId);
            if (enrollmentOptional.isEmpty()) {
                logger.warn("Enrollment not found with ID: {}", enrollmentId);
                return ResponseEntity.notFound().build();
            }

            Enrollment enrollment = enrollmentOptional.get();
            String statusStr = statusData.get("status");
            try {
                enrollment.setStatus(Enrollment.EnrollmentStatus.valueOf(statusStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                logger.error("Invalid status value: {}", statusStr);
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Invalid status value: " + statusStr));
            }

            Enrollment updatedEnrollment = enrollmentRepository.save(enrollment);
            logger.info("Enrollment status updated successfully to {}", updatedEnrollment.getStatus());

            return ResponseEntity.ok(EnrollmentResponseDTO.fromEntity(updatedEnrollment));
        } catch (Exception e) {
            logger.error("Failed to update enrollment status: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Failed to update enrollment status: " + e.getMessage()));
        }
    }

    // Test Google Sheets connection
    @GetMapping("/test-sheets")
    public ResponseEntity<?> testGoogleSheetsConnection() {
        logger.info("=== TESTING GOOGLE SHEETS CONNECTION ===");
        try {
            boolean success = googleSheetsService.testConnection();
            if (success) {
                logger.info("Google Sheets connection test successful");
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Google Sheets connection successful"
                ));
            } else {
                logger.warn("Google Sheets connection test failed");
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Google Sheets connection failed - check logs for details"));
            }
        } catch (Exception e) {
            logger.error("Google Sheets connection test error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Google Sheets connection test error: " + e.getMessage()));
        }
    }
}
