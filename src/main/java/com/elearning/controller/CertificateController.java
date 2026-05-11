package com.elearning.controller;

import com.elearning.model.Certificate;
import com.elearning.model.Course;
import com.elearning.model.Enrollment;
import com.elearning.model.User;
import com.elearning.repository.CertificateRepository;
import com.elearning.repository.CourseRepository;
import com.elearning.repository.EnrollmentRepository;
import com.elearning.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/certificates")
@CrossOrigin(origins = "*")
public class CertificateController {

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Certificate>> getUserCertificates(@PathVariable Long userId) {
        try {
            List<Certificate> certificates = certificateRepository.findByUserId(userId);
            return ResponseEntity.ok(certificates);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateCertificate(@RequestBody Map<String, Long> certificateRequest) {
        try {
            Long userId = certificateRequest.get("userId");
            Long courseId = certificateRequest.get("courseId");

            Optional<User> userOpt = userRepository.findById(userId);
            Optional<Course> courseOpt = courseRepository.findById(courseId);

            if (userOpt.isEmpty() || courseOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("User or Course not found!");
            }

            Optional<Enrollment> enrollmentOpt = enrollmentRepository.findByUserIdAndCourseId(userId, courseId);
            if (enrollmentOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("User not enrolled in this course!");
            }

            Enrollment enrollment = enrollmentOpt.get();
            if (enrollment.getProgress() < 100) {
                return ResponseEntity.badRequest().body("Course not completed yet!");
            }

            Certificate certificate = new Certificate();
            certificate.setCertificateNumber(generateCertificateNumber());
            certificate.setUser(userOpt.get());
            certificate.setCourse(courseOpt.get());
            certificate.setIssueDate(LocalDate.now());
            certificate.setVerificationUrl(generateVerificationUrl(certificate.getCertificateNumber()));

            Certificate savedCertificate = certificateRepository.save(certificate);

            return ResponseEntity.ok(savedCertificate);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Certificate generation failed: " + e.getMessage());
        }
    }

    private String generateCertificateNumber() {
        return "CERT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateVerificationUrl(String certificateNumber) {
        return "https://elearning.com/verify/" + certificateNumber;
    }
}
