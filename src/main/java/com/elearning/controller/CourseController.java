package com.elearning.controller;

import com.elearning.model.Course;
import com.elearning.model.Enrollment;
import com.elearning.model.User;
import com.elearning.repository.CourseRepository;
import com.elearning.repository.EnrollmentRepository;
import com.elearning.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "*")
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        try {
            List<Course> courses = courseRepository.findAll();
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCourseById(@PathVariable Long id) {
        try {
            Optional<Course> courseOpt = courseRepository.findById(id);
            if (courseOpt.isPresent()) {
                return ResponseEntity.ok(courseOpt.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/enroll")
    public ResponseEntity<?> enrollInCourse(@RequestBody Map<String, Long> enrollmentRequest) {
        try {
            Long userId = enrollmentRequest.get("userId");
            Long courseId = enrollmentRequest.get("courseId");

            Optional<User> userOpt = userRepository.findById(userId);
            Optional<Course> courseOpt = courseRepository.findById(courseId);

            if (userOpt.isEmpty() || courseOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("User or Course not found!");
            }

            Optional<Enrollment> existingEnrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId.toString());
            if (existingEnrollment.isPresent()) {
                return ResponseEntity.badRequest().body("User already enrolled in this course!");
            }

            Enrollment enrollment = new Enrollment();
            enrollment.setUser(userOpt.get());
            enrollment.setCourse(courseOpt.get());
            enrollment.setEnrollmentDate(LocalDateTime.now());
            enrollment.setStatus(Enrollment.EnrollmentStatus.ACTIVE);
            enrollment.setProgress(0);

            Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

            return ResponseEntity.ok(savedEnrollment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Enrollment failed: " + e.getMessage());
        }
    }
}
