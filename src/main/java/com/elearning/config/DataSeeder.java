package com.elearning.config;

import com.elearning.model.Course;
import com.elearning.model.User;
import com.elearning.repository.CourseRepository;
import com.elearning.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        seedCourses();
    }

    private void seedCourses() {
        List<Course> existingCourses = courseRepository.findAll();

        // Get or create instructor
        User instructor = userRepository.findByEmail("instructor@learnngrow.com")
            .orElseGet(() -> {
                User newInstructor = new User();
                newInstructor.setName("Tech Expert");
                newInstructor.setEmail("instructor@learnngrow.com");
                newInstructor.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH");
                newInstructor.setRole(User.UserRole.INSTRUCTOR);
                return userRepository.save(newInstructor);
            });

        if (existingCourses.isEmpty()) {
            System.out.println("=== Seeding courses data ===");

            Course[] courses = {
                createCourse("CRS-01", "Android (Kotlin + Jetpack)", "mobile", "Intermediate", 12, 15000.0, instructor),
                createCourse("CRS-02", "Design Patterns", "backend", "Advanced", 8, 15000.0, instructor),
                createCourse("CRS-03", "Data Structures & Algorithms", "data-science", "Intermediate", 10, 20000.0, instructor),
                createCourse("CRS-04", "English Speaking Full Course", "education", "Beginner", 6, 10000.0, instructor),
                createCourse("CRS-05", "Hindi Speaking Full Course", "education", "Beginner", 6, 10000.0, instructor),
                createCourse("CRS-06", "Core & Advanced Python with AI", "data-science", "Intermediate", 16, 15000.0, instructor),
                createCourse("CRS-07", "Fast API", "backend", "Advanced", 6, 14999.0, instructor),
                createCourse("CRS-08", "Cloud Computing (AWS)", "backend", "Advanced", 12, 25000.0, instructor),
                createCourse("CRS-09", "System Designing", "backend", "Advanced", 10, 25000.0, instructor),
                createCourse("CRS-10", "Generative AI", "data-science", "Advanced", 14, 35000.0, instructor),
                createCourse("CRS-11", "C Programming", "frontend", "Beginner", 8, 7500.0, instructor),
                createCourse("CRS-12", "C++ Programming", "frontend", "Intermediate", 10, 7500.0, instructor),
                createCourse("CRS-13", "Core Java", "backend", "Intermediate", 12, 7500.0, instructor),
                createCourse("CRS-14", "Academic Tuitions (Class 6th to 10th)", "education", "Beginner", 0, 4999.0, instructor)
            };

            for (Course course : courses) {
                courseRepository.save(course);
                System.out.println("Seeded course: " + course.getCourseCode() + " - " + course.getName());
            }

            System.out.println("=== Successfully seeded " + courses.length + " courses ===");
        } else {
            System.out.println("=== Courses already exist in database (" + existingCourses.size() + " courses) ===");
            for (Course course : existingCourses) {
                System.out.println("Course: " + course.getCourseCode() + " - " + course.getName());
            }
        }
    }

    private Course createCourse(String courseCode, String name, String category, String level, int duration, double price, User instructor) {
        Course course = new Course();
        course.setCourseCode(courseCode);
        course.setName(name);
        course.setDescription("Course description for " + name);
        course.setCategory(category);
        course.setLevel(Course.CourseLevel.valueOf(level.toUpperCase()));
        course.setDuration(duration);
        course.setPrice(price);
        course.setInstructor(instructor);
        course.setImageUrl("https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=600&h=400&fit=crop");
        return course;
    }
}
